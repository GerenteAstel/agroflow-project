from flask import Flask, request, jsonify
import pika
import mariadb
import os
import uuid
from datetime import datetime

app = Flask(__name__)

# Configuración MariaDB
db_config = {
    'host': os.getenv('MARIADB_HOST', 'mariadb-service'),
    'user': os.getenv('MARIADB_USER'),
    'password': os.getenv('MARIADB_PASSWORD'),
    'database': os.getenv('MARIADB_DATABASE', 'facturacion')
}

# Precios por producto
PRECIOS = {
    "Arroz Oro": 120,
    "Café Premium": 300
}

def get_db_connection():
    return mariadb.connect(**db_config)

@app.route('/facturas', methods=['POST'])
def generar_factura():
    data = request.json
    
    # Calcular monto
    producto = data['producto']
    toneladas = float(data['toneladas'])
    monto = toneladas * PRECIOS.get(producto, 100)  # Default $100/ton
    
    # Guardar en MariaDB
    conn = get_db_connection()
    cursor = conn.cursor()
    
    try:
        factura_id = str(uuid.uuid4())
        cursor.execute(
            "INSERT INTO facturas (factura_id, cosecha_id, monto_total, pagado, fecha_emision) "
            "VALUES (?, ?, ?, FALSE, ?)",
            (factura_id, data['cosecha_id'], monto, datetime.now())
        )
        conn.commit()
        
        # Notificar al servicio Central
        # Aquí iría el código para hacer PUT al servicio Central
        
        return jsonify({
            "factura_id": factura_id,
            "monto": monto,
            "status": "generada"
        }), 201
        
    except Exception as e:
        return jsonify({"error": str(e)}), 500
    finally:
        conn.close()

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)