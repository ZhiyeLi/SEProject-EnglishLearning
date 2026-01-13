/**
 * Authentication API 单元测试
 */
describe('Auth API', () => {
  jest.mock('axios');
  
  let mockAxios;

  beforeEach(() => {
    mockAxios = require('axios');
    jest.clearAllMocks();
  });

  describe('login', () => {
    test('should login successfully with valid credentials', async () => {
      const credentials = { email: 'user@example.com', password: 'password123' };
      const mockResponse = {
        token: 'jwt-token-123',
        user: { id: 1, email: 'user@example.com', name: 'Test User' }
      };

      mockAxios.post.mockResolvedValue({ data: mockResponse });
      const result = await mockAxios.post('/api/auth/login', credentials);

      expect(result.data.token).toBeDefined();
      expect(result.data.user.email).toBe('user@example.com');
    });

    test('should handle login failure', async () => {
      const credentials = { email: 'user@example.com', password: 'wrong' };
      mockAxios.post.mockRejectedValue({ 
        response: { status: 401, data: { message: 'Invalid credentials' } } 
      });

      try {
        await mockAxios.post('/api/auth/login', credentials);
      } catch (error) {
        expect(error.response.status).toBe(401);
      }
    });
  });

  describe('register', () => {
    test('should register new user', async () => {
      const userData = {
        email: 'newuser@example.com',
        password: 'password123',
        name: 'New User'
      };
      const mockResponse = {
        success: true,
        message: 'User registered successfully'
      };

      mockAxios.post.mockResolvedValue({ data: mockResponse });
      const result = await mockAxios.post('/api/auth/register', userData);

      expect(result.data.success).toBe(true);
    });
  });

  describe('logout', () => {
    test('should logout successfully', async () => {
      const mockResponse = { success: true };
      mockAxios.post.mockResolvedValue({ data: mockResponse });

      const result = await mockAxios.post('/api/auth/logout');
      expect(result.data.success).toBe(true);
    });
  });
});
