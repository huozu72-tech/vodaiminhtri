package com.example.chieuthu3_ketthucmonhoc.data.database;

import com.example.chieuthu3_ketthucmonhoc.data.database.dao.MenuItemDao;
import com.example.chieuthu3_ketthucmonhoc.data.database.dao.OrderDao;
import com.example.chieuthu3_ketthucmonhoc.data.database.entity.MenuItemEntity;
import com.example.chieuthu3_ketthucmonhoc.data.database.entity.OrderEntity;
import com.example.chieuthu3_ketthucmonhoc.data.database.entity.OrderItemEntity;
import com.example.chieuthu3_ketthucmonhoc.data.repository.IOrderRepository;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderStatus;

import java.util.ArrayList;
import java.util.List;

public class RoomOrderRepository implements IOrderRepository {

    private final AppDatabase database;
    private final OrderDao dao;
    private final MenuItemDao menuDao;

    public RoomOrderRepository(AppDatabase database) {
        this.database = database;
        this.dao = database.orderDao();
        this.menuDao = database.menuItemDao();
    }

    @Override
    public List<Order> getAllOrders() {
        List<OrderEntity> entities = dao.getAll();
        List<Order> result = new ArrayList<>();
        for (OrderEntity entity : entities) {
            result.add(assembleOrder(entity));
        }
        return result;
    }

    @Override
    public Order getOrderById(String orderId) {
        if (orderId == null) return null;
        OrderEntity entity = dao.getById(orderId.trim());
        return entity != null ? assembleOrder(entity) : null;
    }

    @Override
    public List<Order> getOrdersByCustomer(int customerId) {
        List<OrderEntity> entities = dao.getByCustomer(customerId);
        List<Order> result = new ArrayList<>();
        for (OrderEntity entity : entities) {
            result.add(assembleOrder(entity));
        }
        return result;
    }

    @Override
    public Order saveOrder(Order order) {
        OrderEntity entity = OrderEntity.fromDomain(order);
        database.runInTransaction(() -> {
            OrderEntity existing = dao.getById(entity.getOrderId());
            boolean isNewOrder = (existing == null);
            if (isNewOrder) {
                dao.insert(entity);
            } else if (existing.getOrderId().equals(entity.getOrderId())) {
                dao.update(entity);
            } else {
                dao.deleteById(existing.getOrderId());
                dao.insert(entity);
            }
            dao.deleteItems(entity.getOrderId());

            List<OrderItemEntity> itemEntities = new ArrayList<>();
            for (OrderItem oi : order.getItems()) {
                itemEntities.add(OrderItemEntity.fromDomain(oi, entity.getOrderId()));
                if (isNewOrder && oi.getMenuItem() != null && order.getStatus() != OrderStatus.CANCELLED) {
                    MenuItemEntity me = menuDao.getById(oi.getMenuItem().getId());
                    if (me != null) {
                        me.setOrderCount(me.getOrderCount() + oi.getQuantity());
                        menuDao.update(me);
                    }
                }
            }
            dao.insertItems(itemEntities);
        });
        return order;
    }

    @Override
    public boolean updateOrderStatus(String orderId, OrderStatus newStatus) {
        if (orderId == null || newStatus == null) return false;
        String trimmedId = orderId.trim();
        OrderEntity existing = dao.getById(trimmedId);
        if (existing == null) return false;

        boolean wasCancelled = OrderStatus.CANCELLED.name().equals(existing.getStatus());
        int updatedRows = dao.updateStatus(trimmedId, newStatus.name());
        if (updatedRows > 0 && !wasCancelled && newStatus == OrderStatus.CANCELLED) {
            List<OrderItemEntity> itemEntities = dao.getItems(existing.getOrderId());
            for (OrderItemEntity ie : itemEntities) {
                MenuItemEntity me = menuDao.getById(ie.getMenuItemId());
                if (me != null) {
                    me.setOrderCount(Math.max(0, me.getOrderCount() - ie.getQuantity()));
                    menuDao.update(me);
                }
            }
        }
        return updatedRows > 0;
    }

    private Order assembleOrder(OrderEntity entity) {
        List<OrderItemEntity> itemEntities = dao.getItems(entity.getOrderId());
        List<OrderItem> items = new ArrayList<>();
        for (OrderItemEntity ie : itemEntities) {
            MenuItemEntity me = menuDao.getById(ie.getMenuItemId());
            if (me != null) {
                items.add(ie.toDomain(me.toDomain()));
            }
        }
        return new Order(
                entity.getOrderId(),
                entity.getTableNumber(),
                entity.getCustomerId(),
                items,
                entity.getTotalAmount(),
                OrderStatus.valueOf(entity.getStatus()),
                entity.getPaymentMethod(),
                entity.getQrPayloadUrl(),
                entity.getCreatedAt()
        );
    }
}
