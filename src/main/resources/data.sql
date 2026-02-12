-- Securities initialization (Stock market simulation data)
INSERT INTO securities (symbol, name, type, exchange, sector, current_price, previous_close, open_price, high_price, low_price, volume, change_percent, tradable) VALUES
('AAPL', 'Apple Inc.', 'STOCK', 'NASDAQ', 'Technology', 185.50, 184.00, 184.50, 186.00, 183.50, 50000000, 0.82, true),
('GOOGL', 'Alphabet Inc.', 'STOCK', 'NASDAQ', 'Technology', 141.25, 140.00, 140.50, 142.00, 139.50, 25000000, 0.89, true),
('MSFT', 'Microsoft Corporation', 'STOCK', 'NASDAQ', 'Technology', 378.50, 376.00, 377.00, 380.00, 375.00, 22000000, 0.66, true),
('AMZN', 'Amazon.com Inc.', 'STOCK', 'NASDAQ', 'Consumer', 178.25, 177.00, 177.50, 179.00, 176.50, 35000000, 0.71, true),
('TSLA', 'Tesla Inc.', 'STOCK', 'NASDAQ', 'Automotive', 245.00, 242.00, 243.00, 248.00, 240.00, 80000000, 1.24, true),
('META', 'Meta Platforms Inc.', 'STOCK', 'NASDAQ', 'Technology', 505.75, 502.00, 503.00, 508.00, 500.00, 15000000, 0.75, true),
('NVDA', 'NVIDIA Corporation', 'STOCK', 'NASDAQ', 'Technology', 875.50, 868.00, 870.00, 880.00, 865.00, 40000000, 0.86, true),
('JPM', 'JPMorgan Chase & Co.', 'STOCK', 'NYSE', 'Financial', 198.25, 196.50, 197.00, 199.00, 196.00, 8000000, 0.89, true),
('V', 'Visa Inc.', 'STOCK', 'NYSE', 'Financial', 282.50, 280.00, 281.00, 284.00, 279.50, 6000000, 0.89, true),
('JNJ', 'Johnson & Johnson', 'STOCK', 'NYSE', 'Healthcare', 158.75, 157.50, 158.00, 159.50, 157.00, 5000000, 0.79, true);

-- ETF Securities
INSERT INTO securities (symbol, name, type, exchange, sector, current_price, previous_close, open_price, high_price, low_price, volume, change_percent, tradable) VALUES
('SPY', 'SPDR S&P 500 ETF', 'ETF', 'NYSE', 'Index', 502.50, 500.00, 501.00, 504.00, 499.00, 60000000, 0.50, true),
('QQQ', 'Invesco QQQ Trust', 'ETF', 'NASDAQ', 'Index', 438.25, 435.00, 436.00, 440.00, 434.00, 45000000, 0.75, true),
('IWM', 'iShares Russell 2000 ETF', 'ETF', 'NYSE', 'Index', 205.50, 204.00, 204.50, 206.50, 203.50, 25000000, 0.74, true);

-- Demo user (password: demo123)
INSERT INTO users (username, email, password, phone, real_name, risk_level, verified, created_at, updated_at) VALUES
('demo', 'demo@tradesim.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqzuH.VzB6nwYPCPQTCz8gF3rX7Gy', '13800138000', 'Demo Trader', 'MODERATE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Demo account with 100,000 virtual money
INSERT INTO accounts (user_id, account_no, cash_balance, frozen_cash, total_assets, total_profit, profit_rate, status, created_at, updated_at) VALUES
(1, 'TS1000000001DEMO', 100000.00, 0.00, 100000.00, 0.00, 0.0000, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
