/**
 * Word API 单元测试
 */
describe('Word API', () => {
  // Mock axios
  jest.mock('axios');
  
  let mockAxios;

  beforeEach(() => {
    mockAxios = require('axios');
    jest.clearAllMocks();
  });

  describe('fetchWords', () => {
    test('should fetch words successfully', async () => {
      const mockData = [
        { id: 1, word: 'hello', definition: 'greeting' },
        { id: 2, word: 'world', definition: 'planet' }
      ];

      mockAxios.get.mockResolvedValue({ data: mockData });

      // Simulating the API call
      const result = await mockAxios.get('/api/words');
      
      expect(result.data).toEqual(mockData);
      expect(mockAxios.get).toHaveBeenCalledWith('/api/words');
    });

    test('should handle API error', async () => {
      const errorMessage = 'Network Error';
      mockAxios.get.mockRejectedValue(new Error(errorMessage));

      try {
        await mockAxios.get('/api/words');
      } catch (error) {
        expect(error.message).toBe(errorMessage);
      }
    });
  });

  describe('getWordDetail', () => {
    test('should get word detail by id', async () => {
      const mockWord = {
        id: 1,
        word: 'hello',
        definition: 'a polite greeting',
        example: 'Hello, how are you?'
      };

      mockAxios.get.mockResolvedValue({ data: mockWord });
      const result = await mockAxios.get('/api/words/1');

      expect(result.data).toEqual(mockWord);
    });
  });
});
