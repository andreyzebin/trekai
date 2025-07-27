FROM python:3.9-slim

WORKDIR /app

# Install dependencies
RUN pip install requests pyyaml

# Copy script and default YAML
COPY seed.py .
COPY seed.yaml .

# Set default file name via environment variable
ENV SEED_FILE=seed.yaml

# Run script using environment variable
CMD ["sh", "-c", "python seed.py $SEED_FILE"]
