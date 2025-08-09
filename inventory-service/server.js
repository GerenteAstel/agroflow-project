const amqp = require('amqplib');  // Para RabbitMQ
const mysql = require('mysql2/promise');  // Para MySQL

// Conexión a RabbitMQ
async function procesarEventos() {
    const conn = await amqp.connect('amqp://rabbitmq-service');
    const channel = await conn.createChannel();
    
    const COLA = 'cola_inventario';
    await channel.assertQueue(COLA, { durable: true });
    
    console.log(`Escuchando en ${COLA}...`);
    
    channel.consume(COLA, async (msg) => {
        if (msg) {
            const evento = JSON.parse(msg.content.toString());
            
            if (evento.event_type === 'nueva_cosecha') {
                const { producto, toneladas } = evento.payload;
                
                // Conexión a MySQL
                const db = await mysql.createConnection({
                    host: 'mysql-service',
                    user: process.env.MYSQL_USER,
                    password: process.env.MYSQL_PASSWORD,
                    database: 'inventario'
                });
                
                // Actualizar stock
                await db.execute(
                    'UPDATE insumos SET stock = stock - ? WHERE nombre_insumo = ?',
                    [toneladas * 5, `Semilla ${producto}`]
                );
                
                await db.execute(
                    'UPDATE insumos SET stock = stock - ? WHERE nombre_insumo = ?',
                    [toneladas * 2, 'Fertilizante N-PK']
                );
                
                console.log(`Stock actualizado para ${producto}`);
                db.end();
            }
            
            channel.ack(msg);  // Confirma procesamiento
        }
    });
}

procesarEventos().catch(console.error);