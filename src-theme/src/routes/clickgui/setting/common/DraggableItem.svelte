<script>
    import DragIcon from "./icons/DragIcon.svelte";
</script>

<!-- svelte-ignore a11y-no-static-element-interactions -->
<div class="draggable-item" on:dragstart|preventDefault>
    <DragIcon />

    <slot />
</div>

<style lang="scss">
  .draggable-item {
    display: grid;
    grid-template-columns: max-content 1fr;
    column-gap: 10px;
    align-items: center;
    cursor: pointer;
  }
</style>
