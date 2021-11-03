"""
Run a rest API exposing the yolov5s object detection model
"""
import werkzeug
import io
from io import BytesIO

import torch
from PIL import Image


from flask import Flask, request

device = 'cuda' if torch.cuda.is_available() else 'cpu'
print('Using {} device'.format(device))

app = Flask(__name__)

DETECTION_URL = "/test"

@app.route("/")
def showHomePage():
    # response from the server
    return "Flask server connected..."


@app.route(DETECTION_URL, methods=["POST"])
def predict():
    if not request.method == "POST":
        return

    if request.files.get("image"):
        image_file = request.files["image"]

        image_bytes = image_file.read()

        img = Image.open(io.BytesIO(image_bytes))

        PATH = r"C:/Users/User/Desktop/yolov5/best.pt"
        model = torch.hub.load(r'C:/Users/User/Desktop/yolov5', 'custom', path='best.pt', source='local')  # local repo

        torch.save(model.state_dict(), PATH)
        model.load_state_dict(torch.load(PATH))
        model.eval()       # used to deactivate the layer thats has been kept and output the inference of image as expected
        model.conf = 0.45  # confidence threshold (0-1)
        model.iou = 0.35   # NMS IoU threshold (0-1)
        model.classes = None

        results = model(img, size=320)  # reduce size=320 for faster inference
        results.print()  # print results to screen
        #results.show()  # display results
        results.save()  # save as results1.jpg, results2.jpg... etc.

        for img in results.imgs:
            buffered = BytesIO()
            img_base64 = Image.fromarray(img)
            img_base64.save(buffered, format="JPEG")
            #print(img_base64.b64encode(buffered.getvalue()).decode('utf-8')) # base64 encoded image with results
        return results.pandas().xyxy[0].to_json(orient="records")

        

if __name__ == "__main__":
    app.debug = True        # debug=True causes restarting with stat
    app.config['TEMPLATES_AUTO_RELOAD'] = True
    app.run(host="0.0.0.0", port=5000)  
