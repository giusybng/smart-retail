const express = require('express');
const path = require('path');
const bodyParser = require('body-parser');
const morgan = require('morgan');
const cors = require("cors")

// Import base routes
const indexRouter = require('./routes/index');


// Database configuration
const host = 'localhost';
const dbName = 'smart-retail';

const mongoose = require('mongoose');
mongoose.set('useCreateIndex', true);
mongoose.connect('mongodb://' + host + '/' + dbName);

const db = mongoose.connection;
db.on('error', function () {
  console.error('Connection error!')
});
db.once('open', function () {
  console.log('DB connection Ready');
});

// Init express app
const app = express();

// Enable CORS
app.use(cors())

// Setup logger and body parser
app.use(morgan('dev'));
app.use(bodyParser.json());

// Setup static public folder
app.use(express.static(path.join(__dirname, 'public')));

// Setup base routes
app.use('/', indexRouter);

// Catch 404 errors
app.use(function (req, res, next) {
  const err = new Error('Not Found');
  err.status = 404;
  next(err);
});

// Error handler
app.use(function (err, req, res, next) {
  res.status(err.status || 500);
  res.json({
    message: err.message,
    error: err
  });
});

module.exports = app;