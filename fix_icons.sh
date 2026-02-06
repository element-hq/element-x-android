#!/bin/bash
BASE_DIR="/home/fabian/funker-messenger/appicon/funker/src/main/res"

# Function to resize and convert
process_icon() {
  local dir=$1
  local size_legacy=$2
  local size_adaptive=$3
  
  echo "Processing $dir..."
  
  if [ -d "$BASE_DIR/$dir" ]; then
    # Convert ic_launcher.png (legacy)
    if [ -f "$BASE_DIR/$dir/ic_launcher.png" ]; then
       echo "  Converting ic_launcher.png to ${size_legacy}x${size_legacy}"
       convert "$BASE_DIR/$dir/ic_launcher.png" -resize ${size_legacy}x${size_legacy}! "$BASE_DIR/$dir/ic_launcher.png"
    fi
    
    # Convert ic_launcher_round.png (legacy round)
    if [ -f "$BASE_DIR/$dir/ic_launcher_round.png" ]; then
       echo "  Converting ic_launcher_round.png to ${size_legacy}x${size_legacy}"
       convert "$BASE_DIR/$dir/ic_launcher_round.png" -resize ${size_legacy}x${size_legacy}! "$BASE_DIR/$dir/ic_launcher_round.png"
    fi
    
    # Convert ic_launcher_foreground.png (adaptive)
    if [ -f "$BASE_DIR/$dir/ic_launcher_foreground.png" ]; then
       echo "  Converting ic_launcher_foreground.png to ${size_adaptive}x${size_adaptive}"
       convert "$BASE_DIR/$dir/ic_launcher_foreground.png" -resize ${size_adaptive}x${size_adaptive}! "$BASE_DIR/$dir/ic_launcher_foreground.png"
    fi
  else
    echo "Directory $dir does not exist!"
  fi
}

process_icon "mipmap-mdpi" 48 108
process_icon "mipmap-hdpi" 72 162
process_icon "mipmap-xhdpi" 96 216
process_icon "mipmap-xxhdpi" 144 324
process_icon "mipmap-xxxhdpi" 192 432
