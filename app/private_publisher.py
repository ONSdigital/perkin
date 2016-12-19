#!/usr/bin/env python
#   coding: UTF-8

from app.queue_publisher import QueuePublisher

from cryptography.fernet import Fernet


class PrivatePublisher(QueuePublisher):

    @staticmethod
    def encrypt(message, secret):
        """
        Message may be a string or bytes.
        Secret key must be 32 url-safe base64-encoded bytes.

        """
        try:
            f = Fernet(secret)
        except ValueError:
            return None
        try:
            token = f.encrypt(message)
        except TypeError:
            token = f.encrypt(message.encode("utf-8"))
        return token

    @staticmethod
    def decrypt(token, secret):
        """
        Secret key must be 32 url-safe base64-encoded bytes

        Returned value is a string.
        """
        try:
            f = Fernet(secret)
        except ValueError:
            return None
        message = f.decrypt(token)
        return message.decode("utf-8")

    def publish_message(self, message, content_type=None, headers=None, secret=None):
        if not isinstance(secret, bytes):
            secret = secret.encode("ascii")
        token = PrivatePublisher.encrypt(message, secret=secret)
        return super().publish_message(
            token, content_type=content_type, headers=headers
        )
