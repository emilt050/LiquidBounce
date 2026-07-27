<script lang="ts">
    export let size = 10;
</script>

<svg xmlns="http://www.w3.org/2000/svg" width={size} height={size} viewBox="0 0 13.5 13.501" aria-hidden="true" focusable="false">
  <path id="Vereinigungsmenge_3" data-name="Vereinigungsmenge 3" d="M-773.56,394.5l5.06,5Zm-1.381,0-5.06,5Zm.69-.7-.69.7.69-.7L-780,388l5.75,5.8,5.75-5.8-5.75,5.8.69.7Z" transform="translate(781 -387)" fill="rgba(0,0,0,0)" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"/>
</svg>

<style lang="scss">
svg {
    color: #fff;
transition: color .15s ease;

}

  svg:hover {
    color: var(--accent-color);
    filter: drop-shadow(0 0 2px color-mix(in srgb, var(--accent-color) 45%, transparent));
  }
</style>
