# THIS IS NOT OUR CODE, we just copied it
import numpy as np

def read_ppm(filename):
    """Reads a PPM image file and returns a NumPy matrix."""
    with open(filename, 'rb') as f:
        header = f.readline().decode().strip()
        if header != 'P6':
            raise ValueError("Not a PPM P6 format file")

        # Read width, height, and max color value
        width, height = map(int, f.readline().decode().split())
        max_val = int(f.readline().decode().strip())
        print(width, height, max_val)

        # Read pixel data
        raw_data = np.frombuffer(f.read(), dtype=np.uint16)
        image = raw_data.reshape((height, width, 3))  # RGB format
    return image

def apply_jpeg_ls_filter(image):
    """Applies JPEG-LS prediction to the image."""
    height, width, _ = image.shape
    filtered = np.zeros_like(image)

    for y in range(1,height):
        print(y,"/",height,end="\r")
        for x in range(1,width):
            for c in range(3):  # Process R, G, B channels separately
                A = image[y, x - 1, c] if x > 0 else 0
                B = image[y - 1, x, c] if y > 0 else 0
                C = image[y - 1, x - 1, c] if x > 0 and y > 0 else 0
                
                # Apply LOCO-I predictor
                if C >= max(A, B):
                    predicted = min(A, B)
                elif C <= min(A, B):
                    predicted = max(A, B)
                else:
                    predicted = A + B - C
                
                # Calculate residual (difference between actual and predicted value)
                filtered[y, x, c] = np.clip(image[y, x, c] - predicted, 0, 255)

    return filtered

# Example usage
import sys
ppm_image = read_ppm(sys.argv[1])  # Replace with your PPM file path
jpeg_ls_filtered = apply_jpeg_ls_filter(ppm_image)

# Save the output as a new PPM file
def save_ppm(filename, image):
    """Saves a NumPy matrix as a PPM image."""
    height, width, _ = image.shape
    with open(filename, 'wb') as f:
        f.write(f"P6\n{width} {height}\n255\n".encode())
        f.write(image.tobytes())

save_ppm("output.ppm", jpeg_ls_filtered)
