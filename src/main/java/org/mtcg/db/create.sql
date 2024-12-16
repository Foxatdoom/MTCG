-- Connect to the mtcg_db database
\c public;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA public; -- in der console ausführen falls es nicht geht

-- Drop existing tables if they exist (for fresh setup)
DROP TABLE IF EXISTS battle CASCADE;
DROP TABLE IF EXISTS deck CASCADE;
DROP TABLE IF EXISTS stack_card CASCADE;
DROP TABLE IF EXISTS package CASCADE;
DROP TABLE IF EXISTS package_card CASCADE;
DROP TABLE IF EXISTS stack CASCADE;
DROP TABLE IF EXISTS card CASCADE;
DROP TABLE IF EXISTS "user" CASCADE;

-- id UUID PRIMARY KEY DEFAULT uuid_generate_v4()

-- Create a table for User
CREATE TABLE "user" (
    user_id UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    token VARCHAR(100) NOT NULL,
    coins INTEGER DEFAULT 20 NOT NULL CHECK (coins >= 0)
);

-- Create a table for Card
CREATE TABLE card (
    card_id UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    damage FLOAT NOT NULL CHECK (damage >= 0),
    element_type VARCHAR(50) NOT NULL CHECK (element_type IN ('Fire', 'Water', 'Normal')),
    card_type VARCHAR(20) NOT NULL CHECK (card_type IN ('Spell', 'Monster'))
);

-- Create a table for Stack (User's collection of cards)
CREATE TABLE stack (
    stack_id UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    user_id UUID REFERENCES "user"(user_id) ON DELETE CASCADE,
    UNIQUE(user_id) -- A user can have only one stack
);

-- Create a junction table for Cards in a Stack
CREATE TABLE stack_card (
    stack_id UUID REFERENCES stack(stack_id) ON DELETE CASCADE,
    card_id UUID REFERENCES card(card_id) ON DELETE CASCADE,
    PRIMARY KEY (stack_id, card_id)
);

-- Create a table for Package
CREATE TABLE package (
    package_id UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    user_id UUID REFERENCES "user"(user_id) ON DELETE CASCADE
);

-- Create a junction table for Cards in a Package
CREATE TABLE package_card (
    package_id UUID REFERENCES package(package_id) ON DELETE CASCADE,
    card_id UUID REFERENCES card(card_id) ON DELETE CASCADE,
    PRIMARY KEY (package_id, card_id)
);

-- Create a table for Deck (Best 4 cards selected by the user -> in logic)
CREATE TABLE deck (
    deck_id UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    user_id UUID REFERENCES "user"(user_id) ON DELETE CASCADE,
    card_id UUID REFERENCES card(card_id) ON DELETE CASCADE,
    UNIQUE(user_id, card_id)
);

-- Create a table for Battle
CREATE TABLE battle (
    battle_id UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    user_1 UUID REFERENCES "user"(user_id) ON DELETE CASCADE,
    user_2 UUID REFERENCES "user"(user_id) ON DELETE CASCADE,
    CHECK (user_1 <> user_2) -- Ensure that users in a battle are not the same
);
