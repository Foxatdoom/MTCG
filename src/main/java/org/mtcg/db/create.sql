-- Connect to the mtcg_db database
\c mtcg_db;

-- Drop existing tables if they exist (for fresh setup)
DROP TABLE IF EXISTS battles CASCADE;
DROP TABLE IF EXISTS decks CASCADE;
DROP TABLE IF EXISTS stack_cards CASCADE;
DROP TABLE IF EXISTS packages CASCADE;
DROP TABLE IF EXISTS package_cards CASCADE;
DROP TABLE IF EXISTS stacks CASCADE;
DROP TABLE IF EXISTS cards CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Create a table for Users
CREATE TABLE users (
                       user_id SERIAL PRIMARY KEY,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       password VARCHAR(100) NOT NULL,
                       coins INTEGER DEFAULT 20 NOT NULL CHECK (coins >= 0)
);

-- Create a table for Cards
CREATE TABLE cards (
                       card_id SERIAL PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       damage INTEGER NOT NULL CHECK (damage >= 0),
                       element_type VARCHAR(50) NOT NULL CHECK (element_type IN ('fire', 'water', 'normal')),
                       card_type VARCHAR(20) NOT NULL CHECK (card_type IN ('spell-card', 'monster-card'))
);

-- Create a table for Stacks (User's collection of cards)
CREATE TABLE stacks (
                        stack_id SERIAL PRIMARY KEY,
                        user_id INTEGER REFERENCES users(user_id) ON DELETE CASCADE,
                        UNIQUE(user_id) -- A user can have only one stack
);

-- Create a junction table for Cards in a Stack
CREATE TABLE stack_cards (
                             stack_id INTEGER REFERENCES stacks(stack_id) ON DELETE CASCADE,
                             card_id INTEGER REFERENCES cards(card_id) ON DELETE CASCADE,
                             PRIMARY KEY (stack_id, card_id)
);

-- Create a table for Packages
CREATE TABLE packages (
                          package_id SERIAL PRIMARY KEY,
                          user_id INTEGER REFERENCES users(user_id) ON DELETE CASCADE
);

-- Create a junction table for Cards in a Package
CREATE TABLE package_cards (
                               package_id INTEGER REFERENCES packages(package_id) ON DELETE CASCADE,
                               card_id INTEGER REFERENCES cards(card_id) ON DELETE CASCADE,
                               PRIMARY KEY (package_id, card_id)
);

-- Create a table for Decks (Best 4 cards selected by the user)
CREATE TABLE decks (
                       deck_id SERIAL PRIMARY KEY,
                       user_id INTEGER REFERENCES users(user_id) ON DELETE CASCADE,
                       card_id INTEGER REFERENCES cards(card_id) ON DELETE CASCADE,
                       UNIQUE(user_id, card_id) -- A user can have only one instance of a card in their deck
    -- Note: Limiting to 4 cards in the deck should be handled by application logic
);

-- Create a table for Battles
CREATE TABLE battles (
                         battle_id SERIAL PRIMARY KEY,
                         user_1 INTEGER REFERENCES users(user_id) ON DELETE CASCADE,
                         user_2 INTEGER REFERENCES users(user_id) ON DELETE CASCADE,
                         CHECK (user_1 <> user_2) -- Ensure that users in a battle are not the same
);
