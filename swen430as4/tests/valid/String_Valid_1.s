
	.text
wl_has:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
	movq $0, %rax
	movq %rax, -8(%rbp)
	movq $0, %rax
	movq %rax, 16(%rbp)
	jmp label408
label408:
	movq %rbp, %rsp
	popq %rbp
	ret
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
	movq %rax, %rdi
	call assertion
	movq %rax, %rdi
	call assertion
	subq $32, %rsp
	movq -8(%rbp), %rax
	movq %rax, 8(%rsp)
	movq $48, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $5, %rbx
	movq %rbx, 0(%rax)
	movq %rax, 16(%rsp)
	movq $72, %rbx
	movq %rbx, 8(%rax)
	movq $101, %rbx
	movq %rbx, 16(%rax)
	movq $108, %rbx
	movq %rbx, 24(%rax)
	movq $108, %rbx
	movq %rbx, 32(%rax)
	movq $111, %rbx
	movq %rbx, 40(%rax)
	call wl_has
	addq $32, %rsp
	movq -32(%rsp), %rax
	movq %rax, %rdi
	call assertion
	movq %rax, %rdi
	call assertion
	movq %rax, %rdi
	call assertion
label409:
	movq %rbp, %rsp
	popq %rbp
	ret
	.globl main
main:
	pushq %rbp
	call wl_main
	popq %rbp
	movq $0, %rax
	ret

	.data
