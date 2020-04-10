CREATE TABLE account (
     id SERIAL PRIMARY KEY,
     email    text NOT NULL UNIQUE,
     password text NOT NULL,
     created_at timestamp with time zone DEFAULT now() NOT NULL,
     updated_at timestamp with time zone DEFAULT now() NOT NULL
);

CREATE TABLE profile (
     id SERIAL PRIMARY KEY,
     account_id INTEGER NOT NULL UNIQUE REFERENCES account(id),
     display_name text NOT NULL UNIQUE,
     created_at timestamp with time zone DEFAULT now() NOT NULL,
     updated_at timestamp with time zone DEFAULT now() NOT NULL
);
