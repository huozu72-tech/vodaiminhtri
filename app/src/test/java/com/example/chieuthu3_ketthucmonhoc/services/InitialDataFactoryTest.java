package com.example.chieuthu3_ketthucmonhoc.services;

import com.example.chieuthu3_ketthucmonhoc.data.datasource.InitialDataFactory;
import com.example.chieuthu3_ketthucmonhoc.domain.models.AdminAccount;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Customer;
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Order;
import com.example.chieuthu3_ketthucmonhoc.domain.models.OrderItem;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InitialDataFactoryTest {

    private final List<String> knownPlaintextPasswords = Arrays.asList("123456", "admin123", "manager123");

    private List<MenuItem> menuItems;
    private List<Order> orders;

    @Before
    public void setup() {
        menuItems = InitialDataFactory.createInitialMenuItems();
        orders = InitialDataFactory.createInitialOrders(menuItems);
    }

    @Test
    public void menuItemsHaveUniquePositiveIdsWithPositivePricesAndNonBlankText() {
        assertFalse("Thực đơn seed không được rỗng", menuItems.isEmpty());

        Set<Integer> ids = new HashSet<>();
        for (MenuItem item : menuItems) {
            ids.add(item.getId());
            assertTrue(item.getId() > 0);
            assertTrue(item.getPrice() > 0);
            assertTrue(item.getName() != null && !item.getName().trim().isEmpty());
            assertTrue(item.getDescription() != null && !item.getDescription().trim().isEmpty());
        }
        assertEquals("Id món không được trùng", menuItems.size(), ids.size());
    }

    @Test
    public void menuCoversEveryFoodCategory() {
        for (FoodCategory category : FoodCategory.values()) {
            boolean found = false;
            for (MenuItem item : menuItems) {
                if (item.getCategory() == category) {
                    found = true;
                    break;
                }
            }
            assertTrue("Danh mục '" + category.getDisplayName() + "' phải có ít nhất một món seed", found);
        }
    }

    @Test
    public void customersHaveUniquePhonesAndEmailsWithHashedPasswords() {
        List<Customer> customers = InitialDataFactory.createInitialCustomers();
        assertFalse(customers.isEmpty());

        Set<String> phones = new HashSet<>();
        Set<String> emails = new HashSet<>();
        for (Customer customer : customers) {
            phones.add(customer.getPhoneNumber());
            emails.add(customer.getEmail().toLowerCase());

            assertTrue(customer.getPasswordHash() != null && !customer.getPasswordHash().trim().isEmpty());
            assertFalse(
                    "Mật khẩu seed phải được băm, không lưu plaintext",
                    knownPlaintextPasswords.contains(customer.getPasswordHash())
            );
        }

        assertEquals("Số điện thoại khách không được trùng", customers.size(), phones.size());
        assertEquals("Email khách không được trùng (kể cả khác hoa/thường)", customers.size(), emails.size());
    }

    @Test
    public void adminsHaveUniqueUsernamesWithHashedPasswords() {
        List<AdminAccount> admins = InitialDataFactory.createInitialAdmins();
        assertFalse(admins.isEmpty());

        Set<String> usernames = new HashSet<>();
        for (AdminAccount admin : admins) {
            usernames.add(admin.getUsername().toLowerCase());

            assertTrue(admin.getPasswordHash() != null && !admin.getPasswordHash().trim().isEmpty());
            assertFalse(
                    "Mật khẩu quản trị phải được băm, không lưu plaintext",
                    knownPlaintextPasswords.contains(admin.getPasswordHash())
            );
        }

        assertEquals("Username quản trị không được trùng (kể cả khác hoa/thường)", admins.size(), usernames.size());
    }

    @Test
    public void ordersHaveUniqueCodesWithValidShapeAndReferenceExistingMenuItems() {
        assertFalse("Danh sách đơn seed không được rỗng", orders.isEmpty());
        Set<Integer> menuIds = new HashSet<>();
        for (MenuItem item : menuItems) {
            menuIds.add(item.getId());
        }

        Set<String> orderIds = new HashSet<>();
        Pattern pattern = Pattern.compile("#ORD-\\d+");

        for (Order order : orders) {
            assertTrue(
                    "Mã đơn sai định dạng #ORD-<số>: " + order.getOrderId(),
                    pattern.matcher(order.getOrderId()).matches()
            );
            assertTrue(order.getTableNumber() != null && !order.getTableNumber().trim().isEmpty());
            assertFalse(order.getItems().isEmpty());

            for (OrderItem item : order.getItems()) {
                assertTrue(
                        "Đơn " + order.getOrderId() + " trỏ tới món không có trong thực đơn seed: id=" + item.getMenuItem().getId(),
                        menuIds.contains(item.getMenuItem().getId())
                );
            }
            orderIds.add(order.getOrderId());
        }

        assertEquals("Mã đơn seed không được trùng", orders.size(), orderIds.size());
    }

    @Test
    public void orderTotalEqualsTheSumOfItsItemSubtotals() {
        for (Order order : orders) {
            long sumSubtotals = 0;
            for (OrderItem item : order.getItems()) {
                sumSubtotals += item.getSubtotal();
            }
            assertEquals(
                    "Tổng tiền đơn " + order.getOrderId() + " phải bằng tổng subtotal các món",
                    sumSubtotals,
                    order.getTotalAmount()
            );
        }
    }
}
