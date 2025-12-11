#!/usr/bin/env python3
"""
Extract individual frames from a sprite sheet and save as PNG files.
Each frame is extracted from its specific position and resized to 64x64 pixels.
"""

import os
from PIL import Image
import shutil

# Configuration
SPRITESHEET_PATH = "src/main/resources/assets/SkeletonSpriteSheet2.png"
OUTPUT_DIR = "src/main/resources/assets/frames"
OUTPUT_SIZE = 64  # Size to resize frames to (64x64)
FRAME_HEIGHT = 384  # Each frame is 384 pixels tall

# Manual frame positions based on testing
# Each tuple is (frame_number, left_x, right_x)
# Calculated from the padding values that looked good:
# Frame 1-2: 75px padding = starts at 75, width 231px per frame
# Frame 3: no padding = position based on 256px width
# Frame 4: 128px padding = position based on starting at 128
# Frame 5-6: 100px padding = position based on starting at 100

FRAME_POSITIONS = [
    (1, 75, 306),      # Frame 1: 75px padding
    (2, 306, 537),     # Frame 2: 75px padding  
    (3, 512, 768),     # Frame 3: no padding (512 = 256*2)
    (4, 768, 981),     # Frame 4: 128px padding
    (5, 990, 1213),    # Frame 5: 100px padding
    (6, 1213, 1436),   # Frame 6: 100px padding
]

def main():
    # Check if spritesheet exists
    if not os.path.exists(SPRITESHEET_PATH):
        print(f"Error: Spritesheet not found at {SPRITESHEET_PATH}")
        return
    
    # Create output directory if it doesn't exist
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    
    # Load the spritesheet
    try:
        spritesheet = Image.open(SPRITESHEET_PATH)
        print(f"Loaded spritesheet: {spritesheet.size}")
    except Exception as e:
        print(f"Error loading spritesheet: {e}")
        return
    
    print(f"Extracting frames with custom positions...")
    
    for frame_num, left, right in FRAME_POSITIONS:
        # Crop the frame
        frame = spritesheet.crop((left, 0, right, FRAME_HEIGHT))
        
        # Resize to output size
        frame = frame.resize((OUTPUT_SIZE, OUTPUT_SIZE), Image.Resampling.LANCZOS)
        
        # Save as walk frame
        walk_filename = f"walk-{frame_num}.png"
        walk_path = os.path.join(OUTPUT_DIR, walk_filename)
        frame.save(walk_path, "PNG")
        
        print(f"✓ Extracted {walk_filename}: x={left} to x={right} (width: {right-left}px)")
        
        # Copy to idle frame
        idle_filename = f"idle-{frame_num}.png"
        idle_path = os.path.join(OUTPUT_DIR, idle_filename)
        shutil.copy2(walk_path, idle_path)
        print(f"  → Copied to {idle_filename}")
    
    print(f"\n✓ Successfully extracted {len(FRAME_POSITIONS)} frames!")
    print(f"✓ Both walk and idle animations use the same frames")
    print(f"✓ All frames saved to: {OUTPUT_DIR}")

if __name__ == "__main__":
    main()
