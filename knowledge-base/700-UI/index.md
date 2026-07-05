# Angular Frontend UI & Styling Standards (PrimeNG + Tailwind)

This document defines the styling standards, layout structures, localization architectures, and visual design parameters for all client interfaces built using Angular, PrimeNG, and TailwindCSS.

---

## 1. PrimeNG + TailwindCSS Integration

To combine PrimeNG’s rich pre-built component suite with Tailwind’s rapid styling utilities, we utilize the **unstyled mode** with PrimeNG's **Tailwind Preset** or configure class overrides.

### 1.1 Configuration Checklist
1. Enable PrimeNG theme config inside `angular.json` and styles root.
2. In `tailwind.config.js`, configure Tailwind to scan both project files and PrimeNG source components to purge unused classes.

#### `tailwind.config.js` snippet:
```javascript
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
    "./node_modules/primeng/**/*.{html,js,ts}" // Scan PrimeNG components
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          light: '#34d399',  // Radiant emerald green
          DEFAULT: '#059669',// Corporate dark green (Saudi vibe)
          dark: '#064e3b'    // Deep forest green
        },
        slate: {
          850: '#1e293b',
          950: '#0f172a'     // Corporate dark slate for background
        }
      },
      boxShadow: {
        'glass': '0 8px 32px 0 rgba(0, 0, 0, 0.37)'
      }
    }
  },
  plugins: []
}
```

### 1.2 Component Styling Standard
- Avoid writing raw custom CSS rules inside components. 
- Use PrimeNG components and style them dynamically using Tailwind classes through the `styleClass` property.

```html
<!-- Example of styled PrimeNG Button inside a Card wrapper -->
<p-card styleClass="shadow-glass bg-slate-900 border border-slate-800 rounded-2xl p-6">
  <h2 class="text-xl font-bold text-white mb-4">Motor Quote Review</h2>
  <p-button label="Approve Underwriting" 
            styleClass="w-full bg-brand hover:bg-brand-light text-white font-semibold py-3 rounded-lg transition duration-250 ease-in-out">
  </p-button>
</p-card>
```

---

## 2. Localization & RTL (Arabic/English)

The platform must support dual LTR (English) and RTL (Arabic) rendering dynamically.

### 2.1 Dynamic Layout Direction
- The application root must bind the HTML `dir` and `lang` attributes dynamically to the active language.
- Standard libraries: `@ngx-translate/core`.

```typescript
import { Component, OnInit, Renderer2 } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-root',
  template: `<router-outlet></router-outlet>`
})
export class AppComponent implements OnInit {
  constructor(private translate: TranslateService, private renderer: Renderer2) {}

  ngOnInit() {
    this.translate.addLangs(['en', 'ar']);
    this.translate.setDefaultLang('ar'); // Default for Saudi Arabia operations

    this.translate.onLangChange.subscribe((event) => {
      const dir = event.lang === 'ar' ? 'rtl' : 'ltr';
      this.renderer.setAttribute(document.documentElement, 'dir', dir);
      this.renderer.setAttribute(document.documentElement, 'lang', event.lang);
    });
  }
}
```

### 2.2 RTL Layout Conventions
- **Spacings:** Use logical Tailwind spacing utilities (e.g. `ps-4` for padding-start, `pe-4` for padding-end) rather than `pl-4` / `pr-4` to handle directional shifts automatically.
- **Alignments:** Use `text-start` and `text-end` instead of `text-left` and `text-right`.

---

## 3. Premium Design System & Micro-Animations

To achieve a modern, premium aesthetic:
- **Glassmorphism**: Combine dark slate backgrounds (`bg-slate-950`), semi-transparent panels (`bg-slate-900/80 backdrop-blur-md`), and thin borders (`border border-slate-800`).
- **Typography**: Import **Outfit** (English) and **Cairo** (Arabic) from Google Fonts for a clean, premium typography curve.
- **Skeletons**: Use PrimeNG’s `p-skeleton` component to maintain active layout shapes during dynamic API loads.
- **Micro-Animations**: Animate all interactive transitions (hover states, form step switches) using Tailwind’s transition utilities:

```html
<!-- Interactive Form Field Wrapper -->
<div class="group relative rounded-xl border border-slate-850 bg-slate-900/50 p-4 transition-all duration-300 hover:border-brand">
  <label class="text-xs font-semibold text-slate-400 group-hover:text-brand transition-colors duration-300">
    Saudi National ID
  </label>
  <input type="text" 
         class="w-full bg-transparent text-white outline-none pt-1" 
         placeholder="1000000000">
</div>
```
