import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-bill',
  templateUrl: './bill.component.html',
  styleUrls: ['./bill.component.css'],
})
export class BillComponent implements OnInit {
  orderData: any;
  invoiceNumber: string;
  date: string;
  time: string;

  constructor(private router: Router) {
    const navigation = this.router.getCurrentNavigation();
    this.orderData = navigation?.extras.state?.['orderData'];

    this.invoiceNumber = this.generateSequentialInvoiceNumber();

    const now = new Date();
    this.date = now.toLocaleDateString();
    this.time = now.toLocaleTimeString();
  }

  ngOnInit(): void {
    if (!this.orderData) {
      this.router.navigate(['/home']);
      
    }
  }

  private getTodayDate(): string {
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const dd = String(today.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  }

  private generateSequentialInvoiceNumber(): string {
    console.log(this.orderData)
    const todayDate = this.getTodayDate();

    let storedDate = localStorage.getItem('invoiceDate');
    let sequence = localStorage.getItem('invoiceSequence');

    if (!storedDate || storedDate !== todayDate) {
      sequence = '1';
      localStorage.setItem('invoiceDate', todayDate);
    } else {
      sequence = (parseInt(sequence || '0', 10) + 1).toString();
    }

    localStorage.setItem('invoiceSequence', sequence);

    return sequence;
  }

  getName(item: any): string {
    return item.itemName;
  }

  getItemTotal(item: any): number {
    return item.item.itemPrice * item.saleQty;
  }

  getTotal(): number {
    return this.orderData.saleItemsHelpers.reduce(
      (sum: number, item: any) => sum + this.getItemTotal(item),
      0
    );
  }

  printBill(): void {
    window.print();
  }
}
