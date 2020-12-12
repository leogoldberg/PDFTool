import * as React from 'react';
import './Uploader.css'
import ProgressBar from 'react-bootstrap/ProgressBar'
import Alert from 'react-bootstrap/Alert'
import { Document, Page } from 'react-pdf';
import axios from 'axios';

const url = `http://localhost:${process.env.PORT || 5000}`;

class Uploader extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            uploadedFiles: {}, showSameName: false, showWrongExtension: false, isUploading: false, isDownloading: false, isDownloadReady: false, downloadFile: "",
            downloadFileURL: ""
        }
        this.fileInput = React.createRef();
        this.handleFileUpload = this.handleFileUpload.bind(this)
        this.downloader = this.downloader.bind(this)
        this.handleMerge = this.handleMerge.bind(this)
        this.handleDeletePage = this.handleDeletePage.bind(this)
    }

    sleeper(ms) {
        return function (x) {
            return new Promise(resolve => setTimeout(() => resolve(x), ms));
        };
    }


    handleDeletePage(filename, page, index) {
        console.log("deleting " + page)
        // delete thumbnail from ui:
        var thumbnails = this.state.uploadedFiles[filename]

        var pagenum = parseInt(page.split(".")[0], 10)
        // delete page in backend
        axios.put(
            `${url}/${this.props.id}/${filename}/${pagenum}`
        ).then(res => {
            if (thumbnails.length == 1) {
                // remove file from react state completely
                var files = this.state.uploadedFiles
                delete files[filename]
                this.setState({
                    uploadedFiles: files
                })
            } else {
                // decrement label if page number of thumbnail is greater than the page being removed
                thumbnails = thumbnails.map(thumbnail => {
                    // get pagenumber from label
                    var pagenumber = parseInt(thumbnail.label.split(".")[0], 10)
                    if (pagenumber > pagenum) {
                        pagenumber -= 1
                    }

                    return { label: pagenumber + ".jpg", data: thumbnail.data }
                })

                // remove thumbnail
                thumbnails.splice(index, 1)

                // update state
                this.setState(prevState => ({
                    uploadedFiles: {
                        ...prevState.uploadedFiles,
                        [filename]: thumbnails
                    }
                }))
            }

        }
        )

    }


    handleMerge() {
        this.setState({ isDownloading: true })
        console.log("merging ", Object.keys(this.state.uploadedFiles))
        axios.get(`${url}/download/${this.props.id}`,
            {
                headers: {
                    'Content-type': 'application/pdf',
                    'Cache-Control': 'no-cache'
                },
                responseType: 'blob'
            }
        ).then(this.sleeper(1000)).then(res => {
            console.log("succesfully returned")
            // console.log(res.data)
            var pdf = new Blob([res.data], { type: 'application/pdf' })
            console.log(pdf)
            var pdfURL = URL.createObjectURL(pdf)
            console.log(pdfURL)

            this.setState({
                downloadFile: pdf,
                downloadFileURL: pdfURL,
                isDownloadReady: true
            })
        })
            .catch(err => {

            })
    }

    handleFileUpload(event) {
        const allowedTypes = ['pdf', 'png', 'jpeg', 'jpg']
        // get file 
        const file = event.target.files[0]

        const pdfName = file.name.split('.')[0] + '.pdf'

        if (pdfName in this.state.uploadedFiles) {
            this.setState({
                showSameName: true,
                sameName: pdfName.split('.')[0]
            })
        } else if (allowedTypes.indexOf(file.name.split('.')[1]) < 0) {
            this.setState({
                showWrongExtension: true,
            })
        }
        else {

            this.setState({ isUploading: true })

            var formData = new FormData()
            formData.append("file", file)

            // // send upload request to api
            axios.post(`${url}/upload/${this.props.id}`, formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            }).then(res => {
                console.log("updating " + pdfName)
                console.log(res.data.thumbnails)

                // free download url object
                if (this.state.downloadFileURL !== "") {
                    console.log("It's just been revoked " + this.state.downloadFileURL)
                    URL.revokeObjectURL(this.state.downloadFileURL)
                }

                this.setState(prevState => ({
                    uploadedFiles: {
                        ...prevState.uploadedFiles,
                        [pdfName]: res.data.thumbnails
                    },
                    isUploading: false,
                    isDownloadReady: false,
                    isDownloading: false,
                    downloadFile: "",
                    downloadFileURL: "",
                }), () => {
                    console.log("state of " + pdfName)
                    console.log(this.state)
                })
            })
                .catch(err => {

                })
        }
        // reset file input value to allow multiple upload of same file
        event.target.value = null;
    }

    downloader() {
        return (
            <div>
                { this.state.isDownloadReady ?
                    <div className="row justify-content-center">
                        {/* <button className="btn btn-primary" onClick={this.handleDownload}>Download</button> */}
                        <a className="btn btn-primary" download={"merge.pdf"} href={this.state.downloadFileURL}>Download</a>
                    </div> :
                    <div className="flex-row">
                        <ProgressBar animated now={45} />
                    </div>
                }
            </div>
        )
    }

    render() {
        return (
            // this.state.isDownloading ? this.downloader() : this.uploader()
            <div className="container h-100">
                {
                    this.state.showSameName ?
                        <Alert key='same-name-error' variant='danger' onClose={() => this.setState({ showSameName: false })} dismissible>
                            A file with name {this.state.sameName} has already been uploaded!
                    </Alert> : ""
                }
                {
                    this.state.showWrongExtension ?
                        <Alert key='wrong-extension-error' variant='danger' onClose={() => this.setState({ showWrongExtension: false })} dismissible>
                            Error: only .pdf, .jpeg, .jpg and .png extension types are allowed!
                    </Alert> : ""
                }
                <div className='mb-5'>
                    <h1 className='mt-5'>Welcome to PDF Editor</h1>
                    <p className='lead'>
                        Easily convert, combine and edit PDF, JPG and PNG images into one PDF.
                </p>
                    <div className="custom-file">
                        <input type="file" class="custom-file-input" ref={this.fileInput} onChange={this.handleFileUpload} disabled={this.state.isUploading} />
                        <label className="custom-file-label" for="customFile">Upload files</label>
                    </div>
                </div>
                <div className="row p-3 border border-info container mh-80 preview mb-3">
                    <div className="row">
                        {Object.keys(this.state.uploadedFiles).map((filename, index) => {
                            console.log("filename", filename)
                            console.log(this.state.uploadedFiles[filename])
                            return this.state.uploadedFiles[filename].map((thumbnail, thumbnailIndex) => {
                                { console.log(thumbnail) }
                                return (
                                    <div className="thumbnail col-2 py-1">
                                        <div className="card">
                                            <img key={thumbnail.label} src={`data:image/jpg;base64,${thumbnail.data}`} className="img-thumbnail" />
                                            <button type="button" className="close m-1" aria-label="Close" onClick={() => { this.handleDeletePage(filename, thumbnail.label, thumbnailIndex) }}>
                                                <span aria-hidden="true">&times;</span>
                                            </button>
                                        </div>
                                    </div>
                                )
                            }
                            )
                        }
                        )}
                        {/* display uploading spinner while upoading new file */}
                        {this.state.isUploading ? <div className="thumbnail col-2 py-1">
                            <div className="card card-block d-flex ">
                                <div className="card-body justify-content-center text-center">
                                    <div className="spinner-border text-primary" role="status">
                                        <span className="sr-only">Loading...</span>
                                    </div>
                                </div>
                            </div>
                        </div> : ""}
                    </div>
                </div>
                {
                    !this.state.isDownloading ? <div className="row justify-content-center">
                        <button className="btn btn-primary" onClick={this.handleMerge}>Merge</button>
                    </div> : this.downloader()
                }
            </div>
        )
    }
}

export default Uploader