const express = require('express');
const router = express.Router();

const Products = require('../bin/models/products');

router.get('/', function (req, res) {
    Products.find({ 'Id': 1 }).exec((err, products) => {
        if (err) return res.status(500).json({ error: err });
        if (!products) return res.status(404).json({ message: 'Products not found' });
        res.json(products);
    });
});
module.exports = router;