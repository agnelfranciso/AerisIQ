import os
import sys
from PIL import Image

def generate_padded_foreground():
    source_path = r"d:\Programming\aerisiq\Plugin icon - 1 (1).png"
    drawable_dest = r"d:\Programming\aerisiq\AerisIQ\app\src\main\res\drawable\ic_launcher_foreground.png"
    
    if not os.path.exists(source_path):
        print(f"Error: Source file {source_path} not found.")
        sys.exit(1)
        
    print(f"Opening transparent icon: {source_path}")
    img = Image.open(source_path).convert("RGBA")
    
    # Standard canvas size (512x512 pixels)
    canvas_size = 512
    # Android safe zone is 66% (let's use 60% to make it look balanced and prevent any cutoff)
    safe_zone_size = int(canvas_size * 0.60)
    
    # Resize the logo while preserving aspect ratio
    img_w, img_h = img.size
    aspect_ratio = img_w / img_h
    if aspect_ratio > 1:
        new_w = safe_zone_size
        new_h = int(safe_zone_size / aspect_ratio)
    else:
        new_h = safe_zone_size
        new_w = int(safe_zone_size * aspect_ratio)
        
    print(f"Resizing logo to {new_w}x{new_h} to fit the safe-zone boundaries...")
    resized_logo = img.resize((new_w, new_h), Image.Resampling.LANCZOS)
    
    # Create the transparent foreground canvas
    foreground_canvas = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
    
    # Center the resized logo on the transparent canvas
    x_offset = (canvas_size - new_w) // 2
    y_offset = (canvas_size - new_h) // 2
    foreground_canvas.paste(resized_logo, (x_offset, y_offset), resized_logo)
    
    # Save the padded foreground
    foreground_canvas.save(drawable_dest, "PNG")
    print(f"Padded foreground icon saved to: {drawable_dest}")

if __name__ == "__main__":
    generate_padded_foreground()
