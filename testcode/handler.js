const db = require('db.js');
const documentClient = db.documentClient;

const handler = (req, res) => {
  const params = req.body.params;
  documentClient.query(params, function(err, data) {
    if (err) console.log(err);
    else console.log(data);
  });
};

export default handler;
