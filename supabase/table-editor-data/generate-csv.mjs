import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const directory = path.dirname(fileURLToPath(import.meta.url));
const units = ["id,block,floor_number,room_number,unit_code"];
const bills = ["id,unit_id,billing_month,amount,due_date,status,paid_at"];
let sequence = 0;

for (const block of ["A", "B"]) {
  for (let floor = 11; floor <= 40; floor += 1) {
    for (let room = 1; room <= 28; room += 1) {
      sequence += 1;
      const suffix = sequence.toString(16).padStart(12, "0");
      const unitId = `00000000-0000-4000-8000-${suffix}`;
      const billId = `10000000-0000-4000-8000-${suffix}`;
      const unitCode = `${block}-${String(floor).padStart(2, "0")}-${String(room).padStart(2, "0")}`;
      units.push(`${unitId},${block},${floor},${room},${unitCode}`);
      bills.push(`${billId},${unitId},2026-11-01,285.00,2026-11-15,UNPAID,`);
    }
  }
}

fs.writeFileSync(path.join(directory, "condotrack_units.csv"), units.join("\n") + "\n");
fs.writeFileSync(path.join(directory, "condotrack_bills.csv"), bills.join("\n") + "\n");
