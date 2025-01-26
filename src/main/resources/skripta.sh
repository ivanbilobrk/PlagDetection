#!/bin/bash

# Provjeri da je putanja proslijeđena kao argument
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <directory_path>"
    exit 1
fi

# Putanja do direktorija
DIRECTORY=$1

# Pronađi i obriši sve foldere koji se zovu 'images'
find "$DIRECTORY" -type d -name "Images" -exec rm -rf {} +

echo "All 'images' directories have been deleted from $DIRECTORY."

