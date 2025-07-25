FROM python:3.9-slim

WORKDIR /app

# Install dependency
RUN pip install requests

# Copy script
COPY seed.py .

# Run script on container start
CMD ["python", "seed.py"]
