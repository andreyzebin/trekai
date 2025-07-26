import requests
import time
import os
import json

# Get configuration from environment variables
BACKEND_URL = os.getenv("BACKEND_URL", "http://localhost:8082")
ADMIN_USER = os.getenv("ADMIN_USER", "admin")
ADMIN_PASSWORD = os.getenv("ADMIN_PASSWORD", "admin")

def wait_for_backend():
    """Waits for the backend to become available."""
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
    """Gets a JWT token for the admin user."""
    print("Authenticating admin user...")
    auth_payload = {
        "username": ADMIN_USER,
        "password": ADMIN_PASSWORD
    }
    response = requests.post(f"{BACKEND_URL}/api/auth", json=auth_payload)
    response.raise_for_status()
    token = response.json()["token"]
    print("Authentication successful.")
    return f"Bearer {token}"

def seed_data(auth_header):
    """Seeds the initial data."""
    headers = {"Authorization": auth_header, "Content-Type": "application/json"}

    # 1. Create users
    print("Creating users...")
    users_to_create = [
        {"loginName": "dev1", "name": "Developer One", "email": "dev1@jtrac.info", "password": "password"},
        {"loginName": "manager1", "name": "Manager One", "email": "manager1@jtrac.info", "password": "password"}
    ]
    user_ids = {}
    for user_data in users_to_create:
        # Check if user exists
        get_user_res = requests.get(f"{BACKEND_URL}/api/users/{user_data['loginName']}", headers=headers)
        if get_user_res.status_code == 404:
             res = requests.post(f"{BACKEND_URL}/api/users", json=user_data, headers=headers)
             res.raise_for_status()
             user_id = res.json()["id"]
             user_ids[user_data['loginName']] = user_id
             print(f"  - User '{user_data['loginName']}' created with ID: {user_id}")
        else:
             user_id = get_user_res.json()["id"]
             user_ids[user_data['loginName']] = user_id
             print(f"  - User '{user_data['loginName']}' already exists with ID: {user_id}")


    # 2. Create spaces
    print("\nCreating spaces...")
    spaces_to_create = [
        {"prefixCode": "PROJ1", "name": "Project One"},
        {"prefixCode": "PROJ2", "name": "Project Two"}
    ]
    space_ids = {}
    for space_data in spaces_to_create:
        prefix_code = space_data['prefixCode']
        get_space_res = requests.get(f"{BACKEND_URL}/api/spaces/{prefix_code}", headers=headers)
        if get_space_res.status_code == 404:
            res = requests.post(f"{BACKEND_URL}/api/spaces", json=space_data, headers=headers)
            res.raise_for_status()
            space_id = res.json()["id"]
            space_ids[prefix_code] = space_id
            print(f"  - Space '{prefix_code}' created with ID: {space_id}")
        else:
            get_space_res.raise_for_status()
            space_id = get_space_res.json()["id"]
            space_ids[prefix_code] = space_id
            print(f"  - Space '{prefix_code}' already exists with ID: {space_id}")

    # 3. Add custom fields
    print("\nAdding custom fields...")
    priority_field = {
        "name": "CUS_INT_01", "label": "Priority", "type": 1,
        "options": {"1": "High", "2": "Medium", "3": "Low"}
    }
    requests.post(f"{BACKEND_URL}/api/spaces/PROJ1/fields", json=priority_field, headers=headers).raise_for_status()
    print("  - Field 'Priority' added to PROJ1.")

    severity_field = {
        "name": "CUS_INT_02", "label": "Severity", "type": 1,
        "options": {"1": "Critical", "2": "Major", "3": "Minor"}
    }
    requests.post(f"{BACKEND_URL}/api/spaces/PROJ1/fields", json=severity_field, headers=headers).raise_for_status()
    print("  - Field 'Severity' added to PROJ1.")

    customer_field = {"name": "CUS_STR_01", "label": "Customer Name", "type": 2}
    requests.post(f"{BACKEND_URL}/api/spaces/PROJ2/fields", json=customer_field, headers=headers).raise_for_status()
    print("  - Field 'Customer Name' added to PROJ2.")


    # 4. Assign roles
    print("\nAssigning roles...")
    roles_to_assign = [
        {"prefixCode": "PROJ1", "loginName": "dev1", "roleKey": "ROLE_DEVELOPER"},
        {"prefixCode": "PROJ1", "loginName": "manager1", "roleKey": "ROLE_MANAGER"},
        {"prefixCode": "PROJ2", "loginName": "dev1", "roleKey": "ROLE_GUEST"}
    ]
    for role_data in roles_to_assign:
        payload = {"loginName": role_data["loginName"], "roleKey": role_data["roleKey"]}
        res = requests.post(f"{BACKEND_URL}/api/spaces/{role_data['prefixCode']}/users", json=payload, headers=headers)
        res.raise_for_status()
        print(f"  - Assigned '{role_data['roleKey']}' to '{role_data['loginName']}' in space '{role_data['prefixCode']}'")

    # 5. Create items
    print("\nCreating items...")
    item1_payload = {
        "spacePrefix": "PROJ1",
        "summary": "Fix login button alignment",
        "detail": "The login button on the main page is misaligned on Firefox.",
        "assignedToId": user_ids["dev1"]
    }
    res = requests.post(f"{BACKEND_URL}/api/items", json=item1_payload, headers=headers)
    res.raise_for_status()
    item1_id = res.json()["id"]
    print(f"  - Created item 'Fix login button alignment' with ID: {item1_id}")

    item2_payload = {
        "spacePrefix": "PROJ1",
        "summary": "Implement password recovery feature",
        "detail": "Users need a way to recover their password if they forget it.",
        "assignedToId": user_ids["dev1"]
    }
    res = requests.post(f"{BACKEND_URL}/api/items", json=item2_payload, headers=headers)
    res.raise_for_status()
    print(f"  - Created item 'Implement password recovery feature' with ID: {res.json()['id']}")

    # 6. Add comments and updates to an item
    print("\nAdding comments/updates to an item...")
    update_payload = {
        "comment": "I've investigated this. It seems to be a CSS float issue. I'll work on a fix.",
        "assignedToId": user_ids["manager1"],
        "status": 1 # Assuming 1 is some "In Progress" status
    }
    res = requests.put(f"{BACKEND_URL}/api/items/{item1_id}", json=update_payload, headers=headers)
    res.raise_for_status()
    print(f"  - Added comment and updated item ID: {item1_id}")

    print("\nData seeding complete!")

if __name__ == "__main__":
    wait_for_backend()
    token = get_auth_token()
    seed_data(token)
