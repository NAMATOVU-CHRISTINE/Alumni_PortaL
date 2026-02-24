#!/bin/bash

# Script to generate Android launcher icons from a source image
# Usage: ./generate_launcher_icons.sh <source_image>

SOURCE_IMAGE="$1"

if [ -z "$SOURCE_IMAGE" ]; then
    echo "Usage: ./generate_launcher_icons.sh <source_image>"
    echo "Example: ./generate_launcher_icons.sh convocation_logo.png"
    exit 1
fi

if [ ! -f "$SOURCE_IMAGE" ]; then
    echo "Error: Source image '$SOURCE_IMAGE' not found"
    exit 1
fi

# Check if ImageMagick is installed
if ! command -v convert &> /dev/null; then
    echo "Error: ImageMagick is not installed"
    echo "Install it with: sudo apt-get install imagemagick"
    exit 1
fi

echo "Generating launcher icons from $SOURCE_IMAGE..."

# Define icon sizes for each density
declare -A SIZES=(
    ["mdpi"]=48
    ["hdpi"]=72
    ["xhdpi"]=96
    ["xxhdpi"]=144
    ["xxxhdpi"]=192
)

# Generate icons for each density
for density in "${!SIZES[@]}"; do
    size=${SIZES[$density]}
    output_dir="app/src/main/res/mipmap-${density}"
    
    echo "Generating ${density} icons (${size}x${size})..."
    
    # Generate main launcher icon
    convert "$SOURCE_IMAGE" -resize ${size}x${size} -background white -alpha remove -alpha off "${output_dir}/ic_launcher.webp"
    
    # Generate round launcher icon
    convert "$SOURCE_IMAGE" -resize ${size}x${size} -background white -alpha remove -alpha off \
        \( +clone -threshold -1 -negate -fill white -draw "circle $((size/2)),$((size/2)) $((size/2)),0" \) \
        -alpha off -compose copy_opacity -composite "${output_dir}/ic_launcher_round.webp"
    
    # Generate foreground (for adaptive icons)
    convert "$SOURCE_IMAGE" -resize ${size}x${size} -background transparent "${output_dir}/ic_launcher_foreground.webp"
done

echo "✅ Launcher icons generated successfully!"
echo ""
echo "Icons created in:"
echo "  - app/src/main/res/mipmap-mdpi/"
echo "  - app/src/main/res/mipmap-hdpi/"
echo "  - app/src/main/res/mipmap-xhdpi/"
echo "  - app/src/main/res/mipmap-xxhdpi/"
echo "  - app/src/main/res/mipmap-xxxhdpi/"
