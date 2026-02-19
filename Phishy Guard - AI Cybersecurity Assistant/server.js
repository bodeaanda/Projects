const express = require('express');
const cors = require('cors');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const { Sequelize, DataTypes } = require('sequelize');
const path = require('path');
require('dotenv').config();
const fetch = require('node-fetch');
const rateLimit = require('express-rate-limit');
const NodeCache = require('node-cache');

const app = express();
const port = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json());

// Serve static files from the root directory
app.use(express.static(path.join(__dirname)));

// Debug middleware to log all requests
app.use((req, res, next) => {
  console.log(`${req.method} ${req.url}`);
  if (req.body && Object.keys(req.body).length > 0) {
    console.log('Request body:', req.body);
  }
  if (req.headers.authorization) {
    console.log('Auth header present');
  }
  next();
});

// Root route to serve login page
app.get('/', (req, res) => {
  console.log('Serving login page...');
  res.sendFile(path.join(__dirname, 'login.html'));
});

// About page route
app.get('/about', (req, res) => {
  console.log('Serving about page...');
  res.sendFile(path.join(__dirname, 'about.html'));
});

// Dashboard route
app.get('/dashboard.html', (req, res) => {
  console.log('Serving dashboard page...');
  res.sendFile(path.join(__dirname, 'dashboard.html'));
});

// SQLite database configuration
const sequelize = new Sequelize({
  dialect: 'sqlite',
  storage: './database.sqlite',
  logging: false
});

// User Model
const User = sequelize.define('User', {
  email: {
    type: DataTypes.STRING,
    allowNull: false,
    unique: true,
    validate: {
      isEmail: true
    }
  },
  password: {
    type: DataTypes.STRING,
    allowNull: false
  },
  createdAt: {
    type: DataTypes.DATE,
    defaultValue: Sequelize.NOW
  }
});

// Guardian Model
const Guardian = sequelize.define('Guardian', {
  name: {
    type: DataTypes.STRING,
    allowNull: false
  },
  email: {
    type: DataTypes.STRING,
    allowNull: false
  },
  phone: {
    type: DataTypes.STRING,
    allowNull: true
  },
  isActive: {
    type: DataTypes.BOOLEAN,
    defaultValue: true
  }
});

// Relationships
User.hasMany(Guardian);
Guardian.belongsTo(User);

// JWT Secret
const JWT_SECRET = process.env.JWT_SECRET || 'your-secret-key';

// Initialize cache with 5 minute TTL
const flockxCache = new NodeCache({ 
  stdTTL: 300, // 5 minutes
  checkperiod: 60 // Check for expired entries every minute
});

// Create rate limiter
const flockxLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100, // Limit each IP to 100 requests per windowMs
  message: 'Too many requests to Flockx API, please try again later',
  standardHeaders: true,
  legacyHeaders: false
});

// Initialize database
(async () => {
  try {
    await sequelize.sync();
    console.log('Database synchronized successfully');
  } catch (error) {
    console.error('Database synchronization error:', error);
  }
})();

// Routes
app.post('/api/register', async (req, res) => {
  try {
    const { email, password } = req.body;

    // Check if user already exists
    const existingUser = await User.findOne({ where: { email } });
    if (existingUser) {
      return res.status(400).json({ message: 'User already exists' });
    }

    // Hash password
    const salt = await bcrypt.genSalt(10);
    const hashedPassword = await bcrypt.hash(password, salt);

    // Create new user
    const user = await User.create({
      email,
      password: hashedPassword
    });

    // Create JWT token
    const token = jwt.sign({ userId: user.id }, JWT_SECRET, { expiresIn: '24h' });

    res.status(201).json({ token });
  } catch (error) {
    console.error('Registration error:', error);
    res.status(500).json({ message: 'Server error' });
  }
});

app.post('/api/login', async (req, res) => {
  try {
    const { email, password } = req.body;

    // Find user
    const user = await User.findOne({ where: { email } });
    if (!user) {
      return res.status(400).json({ message: 'Invalid credentials' });
    }

    // Verify password
    const isMatch = await bcrypt.compare(password, user.password);
    if (!isMatch) {
      return res.status(400).json({ message: 'Invalid credentials' });
    }

    // Create JWT token
    const token = jwt.sign({ userId: user.id }, JWT_SECRET, { expiresIn: '24h' });

    res.json({ token });
  } catch (error) {
    console.error('Login error:', error);
    res.status(500).json({ message: 'Server error' });
  }
});

// Protected route example
app.get('/api/user', authenticateToken, async (req, res) => {
  try {
    const user = await User.findByPk(req.user.userId, {
      attributes: { exclude: ['password'] }
    });
    res.json(user);
  } catch (error) {
    res.status(500).json({ message: 'Server error' });
  }
});

// Middleware to authenticate JWT token
function authenticateToken(req, res, next) {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];

  if (!token) {
    return res.status(401).json({ message: 'Access denied' });
  }

  jwt.verify(token, JWT_SECRET, (err, user) => {
    if (err) {
      return res.status(403).json({ message: 'Invalid token' });
    }
    req.user = user;
    next();
  });
}

// Guardian routes
app.post('/api/guardians', authenticateToken, async (req, res) => {
  try {
    const { name, email, phone } = req.body;
    const guardian = await Guardian.create({
      name,
      email,
      phone,
      UserId: req.user.userId
    });
    res.json(guardian);
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
});

app.get('/api/guardians', authenticateToken, async (req, res) => {
  try {
    const guardians = await Guardian.findAll({
      where: { UserId: req.user.userId }
    });
    res.json(guardians);
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
});

app.delete('/api/guardians/:id', authenticateToken, async (req, res) => {
  try {
    const guardian = await Guardian.findOne({
      where: {
        id: req.params.id,
        UserId: req.user.id
      }
    });

    if (!guardian) {
      return res.status(404).json({ message: 'Guardian not found' });
    }

    await guardian.destroy();
    res.json({ message: 'Guardian deleted successfully' });
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
});

// Proxy endpoint for Flockx API with improved error handling and caching
app.post('/api/proxy/flockx', authenticateToken, flockxLimiter, async (req, res) => {
  try {
    const { content } = req.body;

    if (!content) {
      return res.status(400).json({
        error: 'Missing required field: content'
      });
    }

    // Generate cache key from request content
    const cacheKey = Buffer.from(JSON.stringify(content)).toString('base64');
    
    // Check cache first
    const cachedResult = flockxCache.get(cacheKey);
    if (cachedResult) {
      console.log('Returning cached Flockx analysis');
      return res.json(cachedResult);
    }

    // Validate Flockx API key
    if (!process.env.FLOCKX_API_KEY) {
      throw new Error('Flockx API key not configured');
    }

    console.log('Making request to Flockx API...');
    const response = await fetch('https://api.flockx.io/analyze', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${process.env.FLOCKX_API_KEY}`,
        'Accept': 'application/json',
        'User-Agent': 'PhishyGuard-Proxy/1.0'
      },
      body: JSON.stringify({ content }),
      timeout: 10000 // 10 second timeout
    });

    // Handle various HTTP status codes
    if (response.status === 429) {
      return res.status(429).json({
        error: 'Rate limit exceeded on Flockx API',
        retryAfter: response.headers.get('retry-after') || '60'
      });
    }

    if (response.status === 401 || response.status === 403) {
      throw new Error('Invalid or expired Flockx API key');
    }

    if (!response.ok) {
      const errorData = await response.json().catch(() => null);
      throw new Error(
        errorData?.message || 
        `Flockx API request failed with status ${response.status}`
      );
    }

    const data = await response.json();

    // Cache successful response
    flockxCache.set(cacheKey, data);

    // Add debug headers in development
    if (process.env.NODE_ENV === 'development') {
      res.setHeader('X-Cache-Status', 'MISS');
      res.setHeader('X-Proxy-Latency', Date.now() - req.startTime);
    }

    res.json(data);
  } catch (error) {
    console.error('Flockx proxy error:', error);

    // Handle different types of errors
    if (error.name === 'AbortError' || error.name === 'TimeoutError') {
      return res.status(504).json({
        error: 'Request to Flockx API timed out',
        retryable: true
      });
    }

    if (error.message.includes('API key')) {
      return res.status(500).json({
        error: 'Flockx API configuration error',
        retryable: false
      });
    }

    if (error.message.includes('fetch failed')) {
      return res.status(502).json({
        error: 'Unable to reach Flockx API',
        retryable: true
      });
    }

    res.status(500).json({ 
      error: 'Internal server error while processing Flockx request',
      message: process.env.NODE_ENV === 'development' ? error.message : undefined,
      retryable: true
    });
  }
});

// Add timing middleware for development
if (process.env.NODE_ENV === 'development') {
  app.use((req, res, next) => {
    req.startTime = Date.now();
    next();
  });
}

// Start server
app.listen(port, () => {
  console.log(`Server running on port ${port}`);
}); 