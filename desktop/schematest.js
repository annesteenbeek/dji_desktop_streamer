var Validator = require('jsonschema').Validator;
var v = new Validator();


var p = {
  "name": "Barack Obama",
  "address": {
    "lines": [ "1600 Pennsylvania Avenue Northwest" ],
    "zip": "DC 20500",
    "city": "Washington",
  },
  "votes": "lots"
};

var addressSchema = {
  "id": "/SimpleAddress",
  "type": "object",
  "properties": {
    "lines": {
      "type": "array",
      "items": {"type": "string"}
    },
    "zip": {"type": "string"},
    "city": {"type": "string"},
    "country": {"type": "string"}
  },
  "required": ["country"]
};

// Person
var schema = {
  "id": "/SimplePerson",
  "type": "object",
  "properties": {
    "name": {"type": "string"},
    "address": {"$ref": "/SimpleAddress"},
    "votes": {"type": "integer", "minimum": 1}
  }
};



var uavSchema = {
  "id": "/UAVSchema",
  "type": "object",
  "properties": {
    "focal_length": {"type": "float", "minimum": 0},
    "sensor_width": {"type": "integer", "minimum": 0},
    "sensor_height": {"type": "integer", "minimum": 0},
    "image_width": {"type": "integer", "minimum": 0},
    "image_height": {"type": "integer", "minimum": 0}
  }
}
 
pp = {"a": p, "b": p}
pp["a"].name = 3

v.addSchema(addressSchema, '/SimpleAddress');
console.log(v.validate(p, schema));

// console.log('Hello');