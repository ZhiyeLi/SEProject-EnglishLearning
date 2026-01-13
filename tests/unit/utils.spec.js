/**
 * 工具函数单元测试
 */

describe('Utility Functions', () => {
  describe('String Utilities', () => {
    test('should capitalize first letter', () => {
      const capitalize = (str) => str.charAt(0).toUpperCase() + str.slice(1);
      expect(capitalize('hello')).toBe('Hello');
      expect(capitalize('world')).toBe('World');
    });

    test('should convert to kebab-case', () => {
      const toKebabCase = (str) => str.replace(/([a-z])([A-Z])/g, '$1-$2').toLowerCase();
      expect(toKebabCase('helloWorld')).toBe('hello-world');
      expect(toKebabCase('userName')).toBe('user-name');
    });
  });

  describe('Array Utilities', () => {
    test('should find unique items in array', () => {
      const unique = (arr) => [...new Set(arr)];
      expect(unique([1, 2, 2, 3, 3, 3])).toEqual([1, 2, 3]);
      expect(unique(['a', 'b', 'a'])).toEqual(['a', 'b']);
    });

    test('should flatten nested array', () => {
      const flatten = (arr) => arr.reduce((acc, val) => acc.concat(val), []);
      expect(flatten([[1, 2], [3, 4]])).toEqual([1, 2, 3, 4]);
    });
  });

  describe('Number Utilities', () => {
    test('should calculate percentage', () => {
      const percentage = (value, total) => ((value / total) * 100).toFixed(2);
      expect(percentage(25, 100)).toBe('25.00');
      expect(percentage(1, 3)).toBe('33.33');
    });

    test('should format large numbers', () => {
      const formatNumber = (num) => new Intl.NumberFormat('en-US').format(num);
      expect(formatNumber(1000000)).toBe('1,000,000');
      expect(formatNumber(999)).toBe('999');
    });
  });
});
