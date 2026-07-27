<script lang="ts">
    export let width = 5;
    export let height = 15;
    export let active = false;
</script>

<svg
        width={width}
        height={height}
        viewBox="13.5 10.5 9 15"
        fill="currentColor"
        xmlns="http://www.w3.org/2000/svg"
        class:active
        aria-hidden="true"
        focusable="false"
>
    <circle cx="15" cy="12" r="1.5"/>
    <circle cx="21" cy="12" r="1.5"/>

    <circle cx="15" cy="18" r="1.5"/>
    <circle cx="21" cy="18" r="1.5"/>

    <circle cx="15" cy="24" r="1.5"/>
    <circle cx="21" cy="24" r="1.5"/>
</svg>

<style lang="scss">
  svg {
    color: #fff;
    transition: color .15s ease;

  }

  svg:hover,
  svg.active,
  :global(.sortable-drag svg),
  :global(.sortable-fallback svg) {
    color: var(--accent-color);
    filter: drop-shadow(0 0 2px color-mix(in srgb, var(--accent-color) 45%, transparent));
  }
</style>
