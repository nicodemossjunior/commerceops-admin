CREATE INDEX idx_product_stock_quantity ON product (stock_quantity);
CREATE INDEX idx_product_status_stock_quantity ON product (status, stock_quantity);
