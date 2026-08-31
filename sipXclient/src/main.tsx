import React from 'react';
import { createRoot } from 'react-dom/client';
import { App } from './App';
import './styles.css';
import { SipxClientApplication } from './application/sipxClientApplication';

const sipxClientApplication = new SipxClientApplication();

const rootElement = document.getElementById('root');
if (!rootElement) {
  throw new Error('Missing root element');
}

createRoot(rootElement).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
