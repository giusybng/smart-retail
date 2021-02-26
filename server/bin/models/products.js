const mongoose = require('mongoose');

const productSchema = mongoose.Schema({
    Id: Number,
    Name: String,
    Category: String,
    BeaconId: Number
});

const Products = mongoose.model('Products', productSchema, 'products');

module.exports = Products;