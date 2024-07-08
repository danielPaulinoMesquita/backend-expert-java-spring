db = db.getSiblingDB('helpdesk');

db.createCollection('user', {
    validator: {
        $jsonSchema: {
            bsonType: 'object',
            required: ['name', 'email', 'password', 'profiles'],
            properties: {
                _id: {
                    bsonType: 'objectId',
                    description: 'must be an ObjectId and is required'
                },
                name: {
                    bsonType: 'string',
                    description: 'must be a string and is required'
                },
                email: {
                    bsonType: 'string',
                    description: 'must be a string and is required'
                },
                password: {
                    bsonType: 'string',
                    description: 'must be a string and is required'
                },
                profiles: {
                    bsonType: 'array',
                    description: 'must be an array and is required'
                }
            }
        }
    }
});

db.users.createIndex({ email: 1 }, { unique: true });

db.users.insertOne({
    _id: ObjectId("6137f7d4b0b1c65d18a3a5a1"),
    name: "John Doe",
    email: "john.doe@example.com",
    password: "password123",
    profiles: ["ADMIN", "USER"]
});

db.users.insertOne({
    _id: ObjectId("6137f7d4b0b1c65d18a3a5a2"),
    name: "Jane Smith",
    email: "jane.smith@example.com",
    password: "password456",
    profiles: ["USER"]
});