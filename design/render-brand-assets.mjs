import fs from "node:fs";
import path from "node:path";
import { createRequire } from "node:module";

const require = createRequire(import.meta.url);
const sharp = require("C:/Users/nicho/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/sharp");

const projectRoot = "C:/AndroidProject/Condominium_Maintenance_System";
const designDir = path.join(projectRoot, "design");
const resDir = path.join(projectRoot, "app/src/main/res");
const outputDir = "C:/Users/nicho/Documents/Codex/2026-11-06/i/outputs";

const iconSvg = fs.readFileSync(path.join(designDir, "condotrack-icon.svg"));
const logoSvg = fs.readFileSync(path.join(designDir, "condotrack-logo.svg"));
const roundIconSvg = Buffer.from(
  iconSvg
    .toString("utf8")
    .replace(
      '<rect width="512" height="512" rx="118" fill="url(#background)"/>',
      '<circle cx="256" cy="256" r="256" fill="url(#background)"/>'
    )
);

const densities = [
  ["mdpi", 48],
  ["hdpi", 72],
  ["xhdpi", 96],
  ["xxhdpi", 144],
  ["xxxhdpi", 192]
];

for (const [density, size] of densities) {
  const directory = path.join(resDir, `mipmap-${density}`);
  await sharp(iconSvg)
    .resize(size, size)
    .webp({ quality: 100 })
    .toFile(path.join(directory, "ic_launcher.webp"));
  await sharp(roundIconSvg)
    .resize(size, size)
    .webp({ quality: 100 })
    .toFile(path.join(directory, "ic_launcher_round.webp"));
}

fs.mkdirSync(outputDir, { recursive: true });
await sharp(iconSvg)
  .resize(1024, 1024)
  .png()
  .toFile(path.join(outputDir, "CondoTrack_App_Icon_1024.png"));
await sharp(logoSvg)
  .resize({ width: 1800 })
  .png()
  .toFile(path.join(outputDir, "CondoTrack_Logo.png"));

console.log("Rendered CondoTrack launcher icons and brand previews.");
