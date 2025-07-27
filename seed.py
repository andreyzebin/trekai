import requests
import time
import os
import yaml
import sys

BACKEND_URL = os.getenv("BACKEND_URL", "http://localhost:8082")
ADMIN_USER = os.getenv("ADMIN_USER", "admin")
ADMIN_PASSWORD = os.getenv("ADMIN_PASSWORD", "admin")

def wait_for_backend():
    print(f"Waiting for backend at {BACKEND_URL}...")
    while True:
        try:
            response = requests.get(f"{BACKEND_URL}/swagger-ui/index.html")
            if response.status_code == 200:
                print("Backend is up!")
                return
        except requests.exceptions.ConnectionError:
            pass
        time.sleep(2)

def get_auth_token():
    print("Authenticating admin user...")
    auth_payload = {"username": ADMIN_USER, "password": ADMIN_PASSWORD}
    response = requests.post(f"{BACKEND_URL}/api/auth", json=auth_payload)
    response.raise_for_status()
    token = response.json()["token"]
    return f"Bearer {token}"

def load_yaml_data(path):
    with open(path, "r", encoding="utf-8") as f:
        return yaml.safe_load(f)

def seed_data(auth_header, data):
    headers = {"Authorization": auth_header, "Content-Type": "application/json"}
    user_ids = {}
    space_ids = {}

    # Users
    print("Creating users...")
    for user in data.get("users", []):
        login = user["loginName"]
        res = requests.get(f"{BACKEND_URL}/api/users/{login}", headers=headers)
        if res.status_code == 404:
            created = requests.post(f"{BACKEND_URL}/api/users", json=user, headers=headers)
            created.raise_for_status()
            user_ids[login] = created.json()["id"]
            print(f"  - Created user: {login}")
        else:
            user_ids[login] = res.json()["id"]
            print(f"  - User exists: {login}")

    # Spaces
    print("\nCreating spaces...")
    for space in data.get("spaces", []):
        prefix = space["prefixCode"]
        res = requests.get(f"{BACKEND_URL}/api/spaces/{prefix}", headers=headers)
        if res.status_code == 404:
            created = requests.post(f"{BACKEND_URL}/api/spaces", json={
                "prefixCode": prefix, "name": space["name"]
            }, headers=headers)
            created.raise_for_status()
            space_ids[prefix] = created.json()["id"]
            print(f"  - Created space: {prefix}")
        else:
            space_ids[prefix] = res.json()["id"]
            print(f"  - Space exists: {prefix}")

        # Fields
        for field in space.get("fields", []):
            res = requests.post(f"{BACKEND_URL}/api/spaces/{prefix}/fields", json=field, headers=headers)
            res.raise_for_status()
            print(f"    - Field added: {field['name']}")

    # Roles
    print("\nAssigning roles...")
    for role in data.get("roles", []):
        payload = {
            "loginName": role["loginName"],
            "roleKey": role["roleKey"]
        }
        res = requests.post(f"{BACKEND_URL}/api/spaces/{role['prefixCode']}/users", json=payload, headers=headers)
        res.raise_for_status()
        print(f"  - Assigned {payload['roleKey']} to {payload['loginName']} in {role['prefixCode']}")

    # Items
    print("\nCreating items...")
    for item in data.get("items", []):
        payload = {
            "spacePrefix": item["spacePrefix"],
            "summary": item["summary"],
            "detail": item["detail"],
            "assignedToId": user_ids[item["assignedTo"]]
        }
        res = requests.post(f"{BACKEND_URL}/api/items", json=payload, headers=headers)
        res.raise_for_status()
        item_id = res.json()["id"]
        print(f"  - Created item: {payload['summary']}")

        # Updates (optional)
        for update in item.get("updates", []):
            update_payload = {
                "comment": update["comment"],
                "assignedToId": user_ids[update["assignedTo"]],
                "status": update["status"]
            }
            res = requests.put(f"{BACKEND_URL}/api/items/{item_id}", json=update_payload, headers=headers)
            res.raise_for_status()
            print(f"    - Added update to item ID {item_id}")

    print("\nSeeding complete.")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python seed.py <path-to-yaml>")
        sys.exit(1)

    yaml_path = sys.argv[1]
    wait_for_backend()
    token = get_auth_token()
    data = load_yaml_data(yaml_path)
    seed_data(token, data)
