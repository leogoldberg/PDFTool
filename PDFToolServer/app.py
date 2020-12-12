import base64
import shutil
# Boiler plate stuff to start the module
import jpype
import jpype.imports
from jpype.types import * 

from datetime import datetime
import codecs
from time import sleep
import os
from subprocess import Popen
from flask import Flask, request, make_response, send_from_directory, abort, send_file, session, flash, redirect, \
    url_for, jsonify
from celery import Celery, chain
from flask_cors import CORS, cross_origin
from werkzeug.utils import secure_filename

app = Flask(__name__)
CORS(app)



# Celery Configuration
app.config['CELERY_BROKER_URL'] = 'pyamqp://leo:admin123@localhost/pdfapi'
app.config['CELERY_RESULT_BACKEND'] = 'rpc://leo:admin123@localhost/pdfapi'

# Initialize Celery
celery = Celery(app.name, broker=app.config['CELERY_BROKER_URL'])
celery.conf.update(app.config)

# File upload information
UPLOAD_FOLDER = './uploads'
THUMBNAIL_FOLDER = 'thumbnails'
DOWNLOAD_FOLDER = 'download'
DOWNLOAD_FILE = 'merge.pdf'
OUTPUT_FOLDER = './output'
ALLOWED_EXTENSIONS = {'pdf', 'png', 'jpg', 'jpeg'}

def init_jvm(jvmpath):
    if jpype.isJVMStarted():
        return
    jpype.startJVM(classpath=[jvmpath])

def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS


def isPdf(filename):
    return filename.rsplit('.', 1)[1].lower() == 'pdf'

def img_2_pdf(inputPath, outputPath):
    # alernative to jpype: popen
    # Popen way:
    # print(outputPath)
    # proc = Popen(["java", "-jar", "PDFTool-jar-with-dependencies.jar",
    #               "img2pdf", inputPath, outputPath])
    # proc.wait()

    # Launch the JVM 
    init_jvm('jars/PDFTool-jar-with-dependencies.jar')
    from com.company import ImageToPDF
    print("celery task started")

    img2pdf = ImageToPDF(inputPath, outputPath)
    img2pdf.convert()
    return 

def pdf_2_thumbnails(inputPath, outputPath):
      # Launch the JVM 
    init_jvm('jars/PDFTool-jar-with-dependencies.jar')
    from com.company import PDFToThumbnails
    print("celery task started")

    pdf2thumbnails = PDFToThumbnails(inputPath, outputPath)
    pdf2thumbnails.createThumbnails()
    return 


@app.route('/upload/<id>', methods=['POST'])
def upload(id):
    # Launch the JVM 
    init_jvm('jars/PDFTool-jar-with-dependencies.jar')
    from com.company import PDFUtils

    print("id ", id)
    # check if the post request has the file part
    print(request.files)
    if 'file' not in request.files:
        flash('No file part')
        return redirect(request.url)
        # if user does not select file, browser also
        # submit an empty part without filename
    file = request.files['file']
    if file.filename == '':
        flash('No selected file')
        return redirect(request.url)
    if file and allowed_file(file.filename):
          # get output dir for this user session id
        outputDir = os.path.join(OUTPUT_FOLDER, id)
        if not os.path.exists(outputDir):
            os.mkdir(outputDir)
            os.mkdir(os.path.join(outputDir, THUMBNAIL_FOLDER))

        filename = file.filename
        file.save(os.path.join(outputDir, filename))

        outputFileName = filename
        inputPath = os.path.join(
            outputDir, filename)

        thumbnailPath = os.path.join(OUTPUT_FOLDER, id, THUMBNAIL_FOLDER, os.path.splitext(file.filename)[0])
        os.mkdir(thumbnailPath)
        # check if uploaded file needs to be converted now
        if not isPdf(filename):
            convertedFlag = 'true'
            # get output file name
            outputFileName = os.path.splitext(file.filename)[0]+'.pdf'
            print("run celery task with " + outputFileName)

            outputPath = os.path.join(
                outputDir, outputFileName)
            # start conversion task
            img_2_pdf(inputPath, outputPath)
            # delete input file
            os.remove(inputPath)
            # convert output to thumbnails
            pdf_2_thumbnails(outputPath, thumbnailPath)
            thumbnails=get_thumbnails(thumbnailPath)
        else: 
            outputPath = os.path.join(
                outputDir, outputFileName)
            pdf_2_thumbnails(outputPath, thumbnailPath)
            thumbnails=get_thumbnails(thumbnailPath)
    
        return jsonify(thumbnails = thumbnails)

@app.route('/download/<id>', methods=['GET'])
def download(id):
     # Launch the JVM 
    init_jvm('jars/PDFTool-jar-with-dependencies.jar')
    from com.company import PDFMerge

    inputPath = os.path.join(OUTPUT_FOLDER, id)
    outputPath = os.path.join(inputPath, DOWNLOAD_FOLDER)
    if os.path.exists(outputPath):
        print("deleting " + os.path.join(outputPath, DOWNLOAD_FILE))
        os.remove(os.path.join(outputPath, DOWNLOAD_FILE))
    else:
        os.mkdir(outputPath)

    pdfMerger = PDFMerge(inputPath, outputPath, DOWNLOAD_FILE)
    pdfMerger.mergeFiles()

    try:
        return send_from_directory(outputPath, filename=DOWNLOAD_FILE, as_attachment=True)
    except FileNotFoundError:
        abort(404)

@app.route('/<id>', methods=['DELETE'])
def delete(id):
    sessionDir = os.path.join(OUTPUT_FOLDER, id)
    if os.path.exists(sessionDir):
        shutil.rmtree(sessionDir)

    res = make_response(jsonify({}), 204)
    return res

# delete page from specified pdf
@app.route('/<id>/<filename>/<page>', methods=['PUT'])
def deletePage(id, filename, page):
    inputPath = os.path.join(OUTPUT_FOLDER, id, filename)
      # Launch the JVM 
    init_jvm('jars/PDFTool-jar-with-dependencies.jar')
    from com.company import PDFUtils
    print("deleting " + " page " + page + " in " + filename)
    PDFUtils.deletePage(inputPath, int(page)-1)

 

    filename = os.path.splitext(filename)[0]
    # check if file got deleted, if so, delete thumbnail folder
    # to avoid name conflicts
    if not os.path.exists(inputPath):
        shutil.rmtree(os.path.join(OUTPUT_FOLDER, id, THUMBNAIL_FOLDER, filename))
    # respond with success
    res = make_response(jsonify({}), 204)
    return res



def get_thumbnails(path):
    thumbnails = list()
    directory = os.listdir(path)
    # sort thumbnails in order of page number
    for file in sorted(directory, key=lambda filename: int(os.path.splitext(filename)[0])):
        data = dict()
        base = os.path.basename(file)
        print(base)
        data["label"] = base
        open_file = open(os.path.join(path, file),'rb')
        image_read = open_file.read()
        image_64_encode = base64.encodebytes(image_read)
        data["data"] = image_64_encode.decode('ascii')
        thumbnails.append(data)  

    return thumbnails

if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True)
    if not os.path.exists(OUTPUT_FOLDER):
        os.mkdir(OUTPUT_FOLDER)
