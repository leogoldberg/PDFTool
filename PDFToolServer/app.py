from datetime import datetime
from time import sleep
import os
from subprocess import Popen
from flask import Flask, request, send_from_directory, render_template, session, flash, redirect, \
    url_for, jsonify
from celery import Celery
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
OUTPUT_FOLDER = './output'
ALLOWED_EXTENSIONS = {'pdf', 'png', 'jpg', 'jpeg'}


def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS


@celery.task()
def img_2_pdf(inputPath, outputDir, outPutFileName):
    print("celery task started")
    outputPath = os.path.join(outputDir, outPutFileName)
    print(outputPath)
    proc = Popen(["java", "-jar", "PDFTool-jar-with-dependencies.jar",
                  "img2pdf", inputPath, outputPath])
    proc.wait()
    return


@app.route('/img2pdf', methods=['POST'])
def img2pdf():
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
        filename = str(datetime.now().time()) + "-" + \
            secure_filename(file.filename)
        file.save(os.path.join(UPLOAD_FOLDER, filename))
        # create an output directory for this operation
        outputDir = str(datetime.now().time()) + "-" + \
            os.path.splitext(filename)[0]

        outputDir = os.path.join(OUTPUT_FOLDER, outputDir)
        os.mkdir(outputDir)
        # get output file name
        outputFileName = os.path.splitext(file.filename)[0]+'.pdf'
        print("run celery task with " + outputFileName)
        # start conversion task
        task = img_2_pdf.apply_async(
            args=[os.path.join(UPLOAD_FOLDER, filename), outputDir, outputFileName])
        while not task.ready():
            sleep(0.5)
        print("completed celery task")
        # return result file
        return send_from_directory(outputDir, filename=outputFileName, as_attachment=True)


if __name__ == '__main__':
    app.run(debug=True)
