
-- Books: prevent negative prices and stock
ALTER TABLE books
    ADD CONSTRAINT chk_book_price CHECK (price >= 0),
    ADD CONSTRAINT chk_book_stock CHECK (stock >= 0);

-- Cart items: quantity must be positive
ALTER TABLE cart_items
    ADD CONSTRAINT chk_cart_item_quantity CHECK (quantity > 0);

-- Orders: total cannot be negative
ALTER TABLE orders
    ADD CONSTRAINT chk_order_total CHECK (total >= 0);

-- Order items: quantity must be positive and price cannot be negative
ALTER TABLE order_items
    ADD CONSTRAINT chk_order_item_quantity CHECK (quantity > 0),
    ADD CONSTRAINT chk_order_item_unit_price CHECK (unit_price >= 0);

ALTER TABLE cart_items
    DROP CHECK chk_cart_item_quantity;

ALTER TABLE cart_items
    ADD CONSTRAINT chk_cart_item_quantity CHECK (quantity BETWEEN 1 AND 99);