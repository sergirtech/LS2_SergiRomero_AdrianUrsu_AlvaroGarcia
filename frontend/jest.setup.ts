import '@testing-library/jest-dom';

import fetchMock from 'jest-fetch-mock';
import { TextDecoder, TextEncoder } from 'util';

// react-router uses TextEncoder, which jsdom doesn't provide.
Object.assign(global, { TextDecoder, TextEncoder });

jest.mock('./src/utils/Env', () => ({
  getEnv: () => ({
    API_BASE_URL: 'other',
    MEDIA_BASE_URL: 'development',
  }),
}));

fetchMock.enableMocks();
