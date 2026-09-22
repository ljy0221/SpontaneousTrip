<script setup>
</script>

<template>
  <footer class="app-footer">
    <div class="container">
      <p>&copy; 갈래? 말래?</p>
    </div>
  </footer>
</template>

<style lang="scss" scoped>
.app-footer {
  background-color: var(--color-background-alt);
  padding: 1.5rem 0;
  margin-top: auto;
  text-align: center;
  color: var(--color-text-muted);
  border-top: 1px solid var(--color-border);
  box-shadow: 0 -2px 4px rgba(0, 0, 0, 0.02);

  p {
    font-size: var(--font-size-sm);
    font-weight: 500;
    letter-spacing: 0.025em;
  }
}
</style>
