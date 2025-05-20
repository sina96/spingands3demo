#!/bin/bash
docker-compose up -d
echo "Waiting for LocalStack to be ready..."
sleep 5
aws --endpoint-url=http://localhost:4566 s3 mb s3://my-images
echo "Bucket 'my-images' created."
