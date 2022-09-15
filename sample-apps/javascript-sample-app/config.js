// reads in config file
const yaml = require('js-yaml');
const fs = require('fs');

// Get cfg or throw error
function create_config(file) {
    try {
        const config = yaml.load(fs.readFileSync(file, 'utf8'));
        return config;
    }
    catch (e) {
    console.log(e);
    }
}

module.exports = {create_config};