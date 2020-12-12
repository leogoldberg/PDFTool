import logo from './logo.svg';
import './App.css';
import Uploader from './Uploader'
import { BrowserRouter as Router, Route } from 'react-router-dom';
import { Component } from 'react';
import { v4 as uuidv4 } from 'uuid';
import axios from 'axios';

const url = `http://localhost:${process.env.PORT || 5000}`;

class App extends Component {

  constructor(props) {
    super(props)
    // generate uuid for this user session
    this.state = { id: uuidv4() }
    this.componentCleanup = this.componentCleanUp.bind(this)
  }

  render() {
    return (
      <div className="container">
        <div className="row justify-content-md-center">
          <Router>
            <Route exact path="/" render={(props) => (

              <Uploader id={this.state.id} />

            )} />
          </Router>
        </div>
        <footer className="footer mt-auto py-3">
          <div className="container text-center">
            <span className="text-muted">All data submitted/uploaded is deleted after 1 hour.</span>
          </div>
        </footer>
      </div >

    );
  }

  componentDidMount() {
    window.addEventListener('beforeunload', this.componentCleanup);
  }

  componentCleanUp() {
    // free resource on api if app closes or refreshes
    axios.delete(`${url}/${this.state.id}`)
  }

  componentWillUnmount() {
    this.componentCleanup();
    window.removeEventListener('beforeunload', this.componentCleanup); // remove the event handler for normal unmounting
  }
}

export default App;
