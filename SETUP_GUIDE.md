# Expense Tracker Setup Guide

## Database Setup

### Step 1: Clean Database Setup
Open MySQL Workbench and run the following script to create a fresh database with proper tables:

```sql
-- Run the create_tables.sql script
SOURCE "D:/java project/java-project/backend/create_tables.sql";
```

Or manually run these commands:

```sql
DROP DATABASE IF EXISTS expense_tracker;
CREATE DATABASE expense_tracker CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Step 2: Start the Backend

1. Kill any existing Java process on port 8080:
```powershell
# Find the process
netstat -ano | findstr :8080
# Kill it (replace PID with actual number)
taskkill /F /PID <PID>
```

2. Navigate to backend folder and start the application:
```powershell
cd "d:\java project\java-project\backend"
.\mvnw.cmd spring-boot:run
```

3. Wait for the message: `Started ExpensetrackerApplication`

4. Verify backend is running:
   - Open browser: http://localhost:8080/api/health/db
   - Should show: "Database connection is working!"

### Step 3: Load Initial Data

After the backend starts successfully (which creates the tables), run in MySQL Workbench:

```sql
USE expense_tracker;

-- Insert roles if not exists
INSERT IGNORE INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_MODERATOR'), ('ROLE_ADMIN');

-- Create admin user (password: Test123!)
INSERT IGNORE INTO users (username, email, password) 
VALUES ('admin', 'admin@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi');

-- Assign admin role
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN';
```

### Step 4: Start the Frontend

Open the frontend in VS Code Live Server:
1. Right-click on `index.html` in the root folder
2. Select "Open with Live Server"
3. It will open at http://127.0.0.1:5501

## How It Works

### User Registration & Login
1. **Registration**: Creates a new user account with ROLE_USER by default
2. **Login**: Returns a JWT token that's stored in localStorage
3. **Authentication**: All API calls include the JWT token in the Authorization header

### User Data Isolation
- Each user's transactions are stored with their user_id
- When a user logs in, they only see their own transactions
- The backend automatically filters all queries by the logged-in user
- Users cannot access or modify other users' data

### Security Features
- Passwords are BCrypt encrypted
- JWT tokens expire after 24 hours
- All transaction endpoints require authentication
- CORS is configured for specific origins only

## Test Accounts

### Admin Account
- Username: `admin`
- Password: `Test123!`

### Create Test Users
You can register new users through the UI:
1. Click "Register" tab
2. Enter username, email, and password
3. Click "Register"
4. Login with the new credentials

## Features

### Per-User Data
- ✅ Each user has their own transactions
- ✅ Transactions are linked to users via user_id
- ✅ Users can only see/edit/delete their own transactions
- ✅ Summary shows only the logged-in user's data

### Transaction Management
- Add income/expense transactions
- View transaction history
- Delete transactions
- See summary (total income, expense, balance)
- All amounts displayed in Indian Rupees (₹)

## Troubleshooting

### Backend won't start
1. Check if port 8080 is in use
2. Ensure MySQL is running
3. Verify database credentials in application.properties
4. Check if database `expense_tracker` exists

### Login fails
1. Ensure backend is running
2. Check browser console for CORS errors
3. Verify you're accessing frontend from allowed origin (5500, 5501)
4. Check if user exists in database

### Transactions not saving
1. Ensure you're logged in (check for token in localStorage)
2. Check backend logs for errors
3. Verify database connection

## API Endpoints

### Authentication (No auth required)
- POST `/api/auth/signup` - Register new user
- POST `/api/auth/signin` - Login

### Transactions (Auth required)
- GET `/api/transactions` - Get user's transactions
- POST `/api/transactions` - Create transaction
- DELETE `/api/transactions/{id}` - Delete transaction
- GET `/api/transactions/summary` - Get summary

### Health Check
- GET `/api/health/db` - Check database connection
