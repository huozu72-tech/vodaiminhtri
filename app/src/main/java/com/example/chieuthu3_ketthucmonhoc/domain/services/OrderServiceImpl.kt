package com.example.chieuthu3_ketthucmonhoc.domain.services

import com.example.chieuthu3_ketthucmonhoc.data.repository.IOrderRepository
import com.example.chieuthu3_ketthucmonhoc.domain.models.CartItem
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderItem
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderStatus
import java.net.URLEncoder

class OrderServiceImpl(
    private val orderRepository: IOrderRepository
) : IOrderService {

    override fun createOrder(
        tableNumber: String,
        customerId: Int?,
        cartItems: List<CartItem>,
        paymentMethod: String
    ): Order {
        require(tableNumber.isNotBlank()) { "Số bàn không được để trống" }
        require(cartItems.isNotEmpty()) { "Giỏ hàng không được rỗng khi tạo đơn" }
        require(cartItems.all { it.quantity > 0 }) { "Số lượng món ăn trong giỏ phải lớn hơn 0" }
        require(cartItems.all { it.menuItem.isAvailable }) { "Một số món trong giỏ hiện không còn khả dụng" }

        val orderCode = generateUniqueOrderCode()
        val orderItems = cartItems.mapIndexed { index, cartItem ->
            OrderItem(
                id = index + 1,
                menuItem = cartItem.menuItem,
                quantity = cartItem.quantity,
                unitPrice = cartItem.menuItem.price
            )
        }

        val totalAmount = orderItems.sumOf { it.subtotal }
        val newOrder = Order(
            orderId = orderCode,
            tableNumber = tableNumber.trim(),
            customerId = customerId,
            items = orderItems,
            totalAmount = totalAmount,
            status = OrderStatus.PENDING,
            paymentMethod = paymentMethod,
            qrPayloadUrl = generatePaymentQrUrlInternal(orderCode, totalAmount)
        )

        return orderRepository.saveOrder(newOrder)
    }

    override fun generatePaymentQrUrl(order: Order): String {
        return generatePaymentQrUrlInternal(order.orderId, order.totalAmount)
    }

    private fun generatePaymentQrUrlInternal(orderId: String, amount: Long): String {
        val bankId = "MBBank"
        val accountNo = "0901234567"
        val template = "compact2"
        val description = URLEncoder.encode("Thanh toan $orderId", "UTF-8")
        return "https://img.vietqr.io/image/$bankId-$accountNo-$template.png?amount=$amount&addInfo=$description"
    }

    /**
     * Sinh mã đơn tăng dần dựa trên mã số lớn nhất hiện có (thay cho mã ngẫu nhiên)
     * để không bao giờ trùng mã đơn seed hay đơn đã tạo — tránh việc saveOrder ghi đè mất đơn cũ.
     */
    private fun generateUniqueOrderCode(): String {
        val maxExistingNumber = orderRepository.getAllOrders()
            .mapNotNull { it.orderId.removePrefix(ORDER_CODE_PREFIX).toLongOrNull() }
            .maxOrNull() ?: DEFAULT_ORDER_NUMBER
        var number = maxExistingNumber + 1
        while (orderRepository.getOrderById("$ORDER_CODE_PREFIX$number") != null) {
            number += 1
        }
        return "$ORDER_CODE_PREFIX$number"
    }

    override fun getOrderById(orderId: String): Order? {
        return orderRepository.getOrderById(orderId)
    }

    override fun getCustomerOrders(customerId: Int): List<Order> {
        return orderRepository.getOrdersByCustomer(customerId)
    }

    override fun getAllOrders(): List<Order> {
        return orderRepository.getAllOrders()
    }

    /**
     * Chỉ cho phép chuyển trạng thái theo luồng hợp lệ:
     * PENDING -> PREPARING -> COMPLETED và PENDING/PREPARING -> CANCELLED.
     * Chặn nhảy bước (PENDING -> COMPLETED), quay lui hay "hồi sinh"
     * đơn đã COMPLETED/CANCELLED.
     */
    override fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Boolean {
        val order = orderRepository.getOrderById(orderId) ?: return false
        if (!isValidStatusTransition(order.status, newStatus)) {
            return false
        }
        return orderRepository.updateOrderStatus(orderId, newStatus)
    }

    override fun cancelOrder(orderId: String): Boolean {
        val order = orderRepository.getOrderById(orderId) ?: return false
        if (order.status != OrderStatus.PENDING && order.status != OrderStatus.PREPARING) {
            return false // Cannot cancel completed or already-cancelled orders
        }
        return orderRepository.updateOrderStatus(orderId, OrderStatus.CANCELLED)
    }

    private fun isValidStatusTransition(current: OrderStatus, next: OrderStatus): Boolean {
        if (current == next) return false
        return when (current) {
            OrderStatus.PENDING -> next == OrderStatus.PREPARING || next == OrderStatus.CANCELLED
            OrderStatus.PREPARING -> next == OrderStatus.COMPLETED || next == OrderStatus.CANCELLED
            OrderStatus.COMPLETED, OrderStatus.CANCELLED -> false // Trạng thái kết thúc, không đổi nữa
        }
    }

    companion object {
        private const val ORDER_CODE_PREFIX = "#ORD-"

        /** Mã số khởi đầu khi chưa có đơn nào: đơn đầu tiên sẽ là #ORD-1000. */
        private const val DEFAULT_ORDER_NUMBER = 999L
    }
}
