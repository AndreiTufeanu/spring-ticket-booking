export const environment = {
  apiGatewayUrl: window.location.hostname === 'localhost' 
    ? 'http://localhost:5000/gateway' 
    : 'https://20.61.253.117/gateway'
};
