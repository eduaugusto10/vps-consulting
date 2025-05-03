-- Inserir parceiros de teste se não existirem
INSERT INTO partner (name, email, document_number, credit_limit, available_credit) 
SELECT 'Empresa ABC Ltda', 'contato@empresaabc.com.br', '12.345.678/0001-00', 50000.00, 50000.00
WHERE NOT EXISTS (
    SELECT 1 FROM partner WHERE name = 'Empresa ABC Ltda'
); 