import json
import logging
from datetime import UTC, datetime

import pika
from pika.adapters.blocking_connection import BlockingChannel
from pika.spec import BasicProperties

from app.config import Settings


class RabbitMQLogHandler(logging.Handler):
    def __init__(self, amqp_url: str, queue_name: str) -> None:
        super().__init__()
        self._amqp_url = amqp_url
        self._queue_name = queue_name
        self._connection: pika.BlockingConnection | None = None
        self._channel: BlockingChannel | None = None

    def _connect(self) -> None:
        if self._channel and self._channel.is_open:
            return

        parameters = pika.URLParameters(self._amqp_url)
        self._connection = pika.BlockingConnection(parameters)
        self._channel = self._connection.channel()
        self._channel.queue_declare(queue=self._queue_name, durable=True)

    def emit(self, record: logging.LogRecord) -> None:
        try:
            self._connect()
            assert self._channel is not None

            payload = {
                "timestamp": datetime.now(UTC).isoformat(),
                "level": record.levelname,
                "logger": record.name,
                "message": record.getMessage(),
                "module": record.module,
                "function": record.funcName,
            }
            body = json.dumps(payload).encode("utf-8")

            self._channel.basic_publish(
                exchange="",
                routing_key=self._queue_name,
                body=body,
                properties=BasicProperties(delivery_mode=2),
            )
        except Exception:
            self.handleError(record)
            self._safe_close()

    def _safe_close(self) -> None:
        if self._channel and self._channel.is_open:
            self._channel.close()
        if self._connection and self._connection.is_open:
            self._connection.close()
        self._channel = None
        self._connection = None

    def close(self) -> None:
        self._safe_close()
        super().close()


def configure_logging(settings: Settings) -> logging.Logger:
    logger = logging.getLogger("pricescout.products")
    logger.setLevel(logging.INFO)
    logger.propagate = False

    if logger.handlers:
        return logger

    formatter = logging.Formatter(
        "%(asctime)s | %(levelname)s | %(name)s | %(message)s",
    )

    stream_handler = logging.StreamHandler()
    stream_handler.setFormatter(formatter)
    logger.addHandler(stream_handler)

    if settings.rabbitmq_enabled:
        rabbit_handler = RabbitMQLogHandler(
            amqp_url=settings.rabbitmq_url,
            queue_name=settings.rabbitmq_queue,
        )
        rabbit_handler.setFormatter(formatter)
        logger.addHandler(rabbit_handler)

    return logger