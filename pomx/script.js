// Sample in-memory data store
let customers = [
    { id: 'C001', name: 'John Smith', phone: '555-0101', address: '123 Main St' },
    { id: 'C002', name: 'Jane Doe', phone: '555-0102', address: '456 Oak Ave' },
    { id: 'C003', name: 'Alice Brown', phone: '555-0103', address: '789 Pine Ln' },
    { id: 'C004', name: 'Bob White', phone: '555-0104', address: '321 Cedar Rd' }
];

// DOM Elements
const customerListBody = document.getElementById('customerList');
const customerIdField = document.getElementById('customerId');
const nameField = document.getElementById('name');
const phoneField = document.getElementById('phone');
const addressField = document.getElementById('address');
const searchField = document.getElementById('search');
const customerTable = document.querySelector('.customer-table'); // Get the table itself

// Sorting state
let currentSortColumn = null;
let currentSortDirection = 'asc'; // 'asc' or 'desc'

// Initial render
renderTable(customers);
addSortListeners();

function renderTable(data) {
    customerListBody.innerHTML = ''; // Clear existing rows
    data.forEach(customer => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${customer.id}</td>
            <td>${customer.name}</td>
            <td>${customer.phone}</td>
        `;
        row.onclick = () => populateForm(customer);
        customerListBody.appendChild(row);
    });
}

function populateForm(customer) {
    customerIdField.value = customer.id;
    nameField.value = customer.name;
    phoneField.value = customer.phone;
    addressField.value = customer.address;
}

function clearForm() {
    customerIdField.value = '';
    nameField.value = '';
    phoneField.value = '';
    addressField.value = '';
    searchField.value = '';
    renderTable(customers); // Reset table to full list
}

function addCustomer() {
    if (!nameField.value || !phoneField.value) {
        alert('Name and phone are required.');
        return;
    }
    const newCustomer = {
        id: 'C' + String(customers.length + 1).padStart(3, '0'), // Generate a simple new ID like C003
        name: nameField.value,
        phone: phoneField.value,
        address: addressField.value
    };
    customers.push(newCustomer);
    renderTable(customers);
    clearForm();
    alert('Customer added successfully!');
}

function updateCustomer() {
    const id = customerIdField.value;
    if (!id) {
        alert('Please select a customer to update.');
        return;
    }
    const customerIndex = customers.findIndex(c => c.id === id);
    if (customerIndex > -1) {
        customers[customerIndex] = {
            id,
            name: nameField.value,
            phone: phoneField.value,
            address: addressField.value
        };
        renderTable(customers);
        alert('Customer updated successfully!');
    } else {
        alert('Customer not found.');
    }
}

function deleteCustomer() {
    const id = customerIdField.value;
    if (!id) {
        alert('Please select a customer to delete.');
        return;
    }
    const customerIndex = customers.findIndex(c => c.id === id);
    if (customerIndex > -1) {
        customers.splice(customerIndex, 1);
        renderTable(customers);
        clearForm();
        alert('Customer deleted successfully!');
    } else {
        alert('Customer not found.');
    }
}

function searchCustomers() {
    const searchTerm = searchField.value.toLowerCase();
    if (!searchTerm) {
        renderTable(customers);
        return;
    }
    const filteredCustomers = customers.filter(c =>
        c.name.toLowerCase().includes(searchTerm) ||
        c.phone.includes(searchTerm) ||
        c.id.toLowerCase().includes(searchTerm)
    );
    renderTable(filteredCustomers);
}

function addSortListeners() {
    const headers = customerTable.querySelectorAll('th');
    headers.forEach(header => {
        const columnKey = header.textContent.toLowerCase().replace(' ', ''); // 'Customer ID' -> 'customerid'
        // Map to actual object keys
        let actualKey;
        if (columnKey === 'customerid') {
            actualKey = 'id';
        } else if (columnKey === 'name') {
            actualKey = 'name';
        } else if (columnKey === 'phone') {
            actualKey = 'phone';
        } else {
            return; // Don't add listener if not a sortable column
        }

        header.style.cursor = 'pointer'; // Indicate sortable
        header.addEventListener('click', () => sortCustomersByColumn(actualKey));
    });
}

function sortCustomersByColumn(columnKey) {
    if (currentSortColumn === columnKey) {
        currentSortDirection = currentSortDirection === 'asc' ? 'desc' : 'asc';
    } else {
        currentSortColumn = columnKey;
        currentSortDirection = 'asc';
    }

    customers.sort((a, b) => {
        const aValue = a[columnKey].toLowerCase();
        const bValue = b[columnKey].toLowerCase();

        if (aValue < bValue) {
            return currentSortDirection === 'asc' ? -1 : 1;
        }
        if (aValue > bValue) {
            return currentSortDirection === 'asc' ? 1 : -1;
        }
        return 0;
    });

    renderTable(customers);
    updateSortIndicators();
}

function updateSortIndicators() {
    const headers = customerTable.querySelectorAll('th');
    headers.forEach(header => {
        header.classList.remove('sort-asc', 'sort-desc');
        const columnKey = header.textContent.toLowerCase().replace(' ', '');
        let actualKey;
        if (columnKey === 'customerid') {
            actualKey = 'id';
        } else if (columnKey === 'name') {
            actualKey = 'name';
        } else if (columnKey === 'phone') {
            actualKey = 'phone';
        }

        if (actualKey === currentSortColumn) {
            header.classList.add('sort-' + currentSortDirection);
        }
    });
}