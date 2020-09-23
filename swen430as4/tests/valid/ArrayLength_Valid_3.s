
	.text
wl_len:
	pushq %rbp
	movq %rsp, %rbp
	movq %rax, 16(%rbp)
	jmp label527
label527:
	movq %rbp, %rsp
	popq %rbp
	ret
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
	subq $16, %rsp
	movq -8(%rbp), %rax
	movq %rax, 8(%rsp)
	call wl_len
	addq $16, %rsp
	movq -16(%rsp), %rax
	movq $3, %rbx
	cmpq %rax, %rbx
	jnz label529
	movq $1, %rax
	jmp label530
label529:
	movq $0, %rax
label530:
	movq %rax, %rdi
	call assertion
	subq $16, %rsp
	movq -8(%rbp), %rax
	movq %rax, 8(%rsp)
	call wl_len
	addq $16, %rsp
	movq -16(%rsp), %rax
	movq $6, %rbx
	cmpq %rax, %rbx
	jnz label531
	movq $1, %rax
	jmp label532
label531:
	movq $0, %rax
label532:
	movq %rax, %rdi
	call assertion
label528:
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
